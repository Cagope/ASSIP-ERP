
package co.assip.erp.contabilidad.comprobantes.importar;

import co.assip.erp.contabilidad.comprobantes.crear.ComprobantesCrearService;
import co.assip.erp.contabilidad.comprobantes.crear.dto.ComprobanteCrearRequestDTO;
import co.assip.erp.contabilidad.comprobantes.importar.dto.ComprobanteImportacionResponseDTO;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuenta;
import co.assip.erp.contabilidad.plan_cuentas.PlanCuentaRepository;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Valida e importa comprobantes manuales desde Excel.
 * Admite cuentas normales y cuentas con control de entrada/salida.
 * La contabilización definitiva utiliza ComprobantesCrearService.
 */
@Service
@RequiredArgsConstructor
public class ComprobantesImportarService {

    private static final String PERMISO = "CONTABILIDAD_NEW";
    private static final long TAMANO_MAXIMO = 10L * 1024 * 1024;
    private static final int MAX_FILAS = 10000;
    private static final int COLUMNAS = 8;

    private final PlanCuentaRepository planCuentaRepository;
    private final ComprobantesImportarRepository importarRepository;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;
    private final ComprobantesCrearService comprobantesCrearService;

    /**
     * Valida agencia, cuentas, terceros, importes y controles documentales.
     * Devuelve los movimientos y errores sin registrar contabilidad.
     */
    @Transactional(readOnly = true)
    public ComprobanteImportacionResponseDTO validar(
            Integer idAgencia,
            MultipartFile archivo
    ) {
        if (idAgencia == null || idAgencia <= 0) {
            throw new IllegalArgumentException(
                    "Seleccione una agencia válida."
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        var usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() ->
                        new SecurityException("Usuario no activo.")
                );

        accessValidator.validarAcceso(usuario, PERMISO);
        usuarioSesionService.validarAgencia(idAgencia);

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Seleccione un archivo Excel."
            );
        }

        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException(
                    "El Excel supera el límite de 10 MB."
            );
        }

        String nombre = archivo.getOriginalFilename() == null
                ? ""
                : archivo.getOriginalFilename().trim();

        String nombreMinuscula = nombre.toLowerCase(Locale.ROOT);

        if (!nombreMinuscula.endsWith(".xlsx")
                && !nombreMinuscula.endsWith(".xls")) {
            throw new IllegalArgumentException(
                    "El archivo debe ser .xlsx o .xls."
            );
        }

        List<ComprobanteImportacionResponseDTO.Movimiento> movimientos =
                new ArrayList<>();

        List<ComprobanteImportacionResponseDTO.ErrorValidacion> errores =
                new ArrayList<>();

        BigDecimal totalDebitos = BigDecimal.ZERO;
        BigDecimal totalCreditos = BigDecimal.ZERO;

        int totalFilas = 0;
        int filasConErrores = 0;

        Map<String, PlanCuenta> cuentas = new HashMap<>();
        Map<String, List<Integer>> terceros = new HashMap<>();

        Map<String, List<ComprobantesImportarRepository.TipoControl>>
                tiposControl = new HashMap<>();

        DataFormatter formatter = new DataFormatter(
                Locale.forLanguageTag("es-CO")
        );

        try (Workbook libro = WorkbookFactory.create(
                archivo.getInputStream()
        )) {
            if (libro.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "El Excel no tiene hojas."
                );
            }

            Sheet hoja = libro.getSheetAt(0);

            FormulaEvaluator evaluador = libro
                    .getCreationHelper()
                    .createFormulaEvaluator();

            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);

                if (fila == null
                        || filaVacia(fila, formatter, evaluador)) {
                    continue;
                }

                totalFilas++;

                if (totalFilas > MAX_FILAS) {
                    throw new IllegalArgumentException(
                            "El Excel supera las 10.000 filas permitidas."
                    );
                }

                int numeroFila = i + 1;
                int erroresAntes = errores.size();

                String codigo = texto(
                        fila, 0, formatter, evaluador
                );

                String documento = texto(
                        fila, 1, formatter, evaluador
                );

                String detalle = texto(
                        fila, 2, formatter, evaluador
                );

                BigDecimal debito = importe(
                        fila, 3, "Débito", numeroFila,
                        formatter, evaluador, errores
                );

                BigDecimal credito = importe(
                        fila, 4, "Crédito", numeroFila,
                        formatter, evaluador, errores
                );

                BigDecimal base = importe(
                        fila, 5, "Monto/Base", numeroFila,
                        formatter, evaluador, errores
                );

                String descripcionControl = texto(
                        fila, 6, formatter, evaluador
                );

                String documentoSoporte = texto(
                        fila, 7, formatter, evaluador
                );

                Integer idCuenta = null;
                Integer idTercero = null;
                Integer idTiposControl = null;

                boolean requiereControl = false;

                // Resuelve la cuenta y determina si requiere control.
                if (codigo.isBlank()) {
                    error(
                            errores, numeroFila, "Cuenta",
                            "La cuenta es obligatoria."
                    );
                } else {
                    PlanCuenta cuenta = cuentas.get(codigo);

                    if (cuenta == null) {
                        cuenta = planCuentaRepository
                                .findByIdAgenciaAndCodigoCuenta(
                                        idAgencia, codigo
                                )
                                .orElse(null);

                        if (cuenta != null) {
                            cuentas.put(codigo, cuenta);
                        }
                    }

                    if (cuenta == null) {
                        error(
                                errores, numeroFila, "Cuenta",
                                "La cuenta no existe en la agencia."
                        );
                    } else if (!Boolean.TRUE.equals(
                            cuenta.getOperable()
                    )) {
                        error(
                                errores, numeroFila, "Cuenta",
                                "La cuenta no es operable."
                        );
                    } else {
                        idCuenta = cuenta.getId();

                        requiereControl = Boolean.TRUE.equals(
                                cuenta.getControlEntradaSalida()
                        );
                    }
                }

                // Resuelve el tercero y detecta documentos ambiguos.
                if (documento.isBlank()) {
                    error(
                            errores, numeroFila, "Tercero",
                            "El tercero es obligatorio en ASSIP ERP."
                    );
                } else {
                    List<Integer> ids = terceros.computeIfAbsent(
                            documento,
                            importarRepository::buscarIdsTerceroPorDocumento
                    );

                    if (ids.isEmpty()) {
                        error(
                                errores, numeroFila, "Tercero",
                                "El tercero no existe."
                        );
                    } else if (ids.size() > 1) {
                        error(
                                errores, numeroFila, "Tercero",
                                "El documento identifica varios terceros."
                        );
                    } else {
                        idTercero = ids.get(0);
                    }
                }

                if (detalle.isBlank()) {
                    error(
                            errores, numeroFila, "Detalle",
                            "El detalle es obligatorio."
                    );
                }

                // Valida el tipo de control según la cuenta contable.
                if (idCuenta != null) {
                    if (requiereControl) {
                        // Asigna CAJA por defecto solo a cuentas que requieren control.
                        if (descripcionControl.isBlank()) {
                            descripcionControl = "CAJA";
                        }

                        String claveControl = descripcionControl
                                .trim()
                                .toUpperCase(Locale.ROOT);

                        List<ComprobantesImportarRepository.TipoControl>
                                coincidencias = tiposControl.computeIfAbsent(
                                claveControl,
                                importarRepository
                                        ::buscarTiposControlPorDescripcion
                        );

                        if (coincidencias.isEmpty()) {
                            error(
                                    errores, numeroFila,
                                    "Tipo de control",
                                    "El tipo de control no existe."
                            );
                        } else if (coincidencias.size() > 1) {
                            error(
                                    errores, numeroFila,
                                    "Tipo de control",
                                    "La descripción identifica varios "
                                            + "tipos de control."
                            );
                        } else {
                            var tipo = coincidencias.get(0);

                            if (tipo.pideDocumento() == null) {
                                error(
                                        errores, numeroFila,
                                        "Tipo de control",
                                        "No está definida la obligatoriedad "
                                                + "del documento soporte."
                                );
                            } else {
                                idTiposControl = tipo.idTiposControl();

                                if (Boolean.TRUE.equals(
                                        tipo.pideDocumento()
                                ) && documentoSoporte.isBlank()) {
                                    error(
                                            errores, numeroFila,
                                            "Documento soporte",
                                            "El tipo de control "
                                                    + tipo.descripcion()
                                                    + " requiere documento soporte."
                                    );
                                }
                            }
                        }

                    } else {
                        if (!descripcionControl.isBlank()) {
                            error(
                                    errores, numeroFila, "Tipo de control",
                                    "La cuenta no requiere control "
                                            + "de entrada/salida."
                            );
                        }

                        if (!documentoSoporte.isBlank()) {
                            error(
                                    errores, numeroFila, "Documento soporte",
                                    "La cuenta no requiere documento "
                                            + "de control."
                            );
                        }
                    }
                }

                // Controla la longitud del documento, incluso si es opcional.
                if (documentoSoporte.length() > 20) {
                    error(
                            errores, numeroFila, "Documento soporte",
                            "El documento soporte supera 20 caracteres."
                    );
                }

                // Comprueba importes y exclusividad de débito o crédito.
                if (debito != null && credito != null) {
                    if (debito.signum() < 0
                            || credito.signum() < 0) {
                        error(
                                errores, numeroFila, "Valores",
                                "No se permiten valores negativos."
                        );
                    }

                    if (debito.signum() > 0
                            && credito.signum() > 0) {
                        error(
                                errores, numeroFila, "Valores",
                                "Una fila no puede tener débito y crédito "
                                        + "simultáneamente."
                        );
                    }

                    if (debito.signum() == 0
                            && credito.signum() == 0) {
                        error(
                                errores, numeroFila, "Valores",
                                "La fila debe tener débito o crédito."
                        );
                    }

                    totalDebitos = totalDebitos.add(debito);
                    totalCreditos = totalCreditos.add(credito);
                }

                if (base != null && base.signum() < 0) {
                    error(
                            errores, numeroFila, "Monto/Base",
                            "La base no puede ser negativa."
                    );
                }

                if (errores.size() > erroresAntes) {
                    filasConErrores++;
                }

                movimientos.add(
                        new ComprobanteImportacionResponseDTO.Movimiento(
                                numeroFila,
                                codigo,
                                idCuenta,
                                documento,
                                idTercero,
                                detalle,
                                debito,
                                credito,
                                base,
                                idTiposControl,
                                documentoSoporte.isBlank()
                                        ? null
                                        : documentoSoporte
                        )
                );
            }

        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "No fue posible leer el archivo Excel.",
                    ex
            );
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException) {
                throw ex;
            }

            throw new IllegalArgumentException(
                    "No fue posible procesar el archivo Excel.",
                    ex
            );
        }

        BigDecimal diferencia = totalDebitos.subtract(
                totalCreditos
        );

        if (totalFilas == 0) {
            error(
                    errores, 0, "Archivo",
                    "No contiene movimientos."
            );
        }

        if (totalFilas < 2) {
            error(
                    errores, 0, "Archivo",
                    "Se requieren al menos dos movimientos."
            );
        }

        if (totalDebitos.signum() <= 0
                || totalCreditos.signum() <= 0) {
            error(
                    errores, 0, "Totales",
                    "Débitos y créditos deben ser positivos."
            );
        }

        if (diferencia.signum() != 0) {
            error(
                    errores, 0, "Totales",
                    "El comprobante está descuadrado."
            );
        }

        return new ComprobanteImportacionResponseDTO(
                nombre,
                idAgencia,
                totalFilas,
                totalFilas - filasConErrores,
                filasConErrores,
                totalDebitos,
                totalCreditos,
                diferencia,
                errores.isEmpty(),
                movimientos,
                errores
        );
    }

    /**
     * Revalida el Excel recibido y crea un único comprobante manual.
     * No acepta movimientos alterados desde el frontend.
     */
    @Transactional
    public String confirmar(
            Integer idAgencia,
            String tipoComprobante,
            LocalDate fechaContable,
            String conceptoComprobante,
            MultipartFile archivo,
            HttpServletRequest request
    ) {
        if (tipoComprobante == null || tipoComprobante.isBlank()) {
            throw new IllegalArgumentException(
                    "Seleccione el tipo de comprobante."
            );
        }

        if (fechaContable == null) {
            throw new IllegalArgumentException(
                    "La fecha contable es obligatoria."
            );
        }

        if (conceptoComprobante == null
                || conceptoComprobante.isBlank()) {
            throw new IllegalArgumentException(
                    "El concepto general es obligatorio."
            );
        }

        ComprobanteImportacionResponseDTO validacion =
                validar(idAgencia, archivo);

        if (!Boolean.TRUE.equals(validacion.puedeGrabar())) {
            throw new IllegalArgumentException(
                    "El Excel contiene errores. Corríjalos "
                            + "y vuelva a validar antes de grabar."
            );
        }

        List<ComprobanteCrearRequestDTO.Detalle> detalles =
                validacion.movimientos()
                        .stream()
                        .map(m -> new ComprobanteCrearRequestDTO.Detalle(
                                m.idCatalogoCuenta(),
                                m.idDatosPersonal(),
                                m.detalleMovimiento(),
                                m.valorDebito(),
                                m.valorCredito(),
                                m.valorBase(),
                                m.idTiposControl(),
                                m.documentoSoporte()
                        ))
                        .toList();

        ComprobanteCrearRequestDTO solicitud =
                new ComprobanteCrearRequestDTO(
                        idAgencia,
                        tipoComprobante,
                        fechaContable,
                        conceptoComprobante,
                        detalles
                );

        return comprobantesCrearService.crear(solicitud, request);
    }

    /**
     * Identifica filas sin información en ninguna de las ocho columnas.
     */
    private boolean filaVacia(
            Row fila,
            DataFormatter formatter,
            FormulaEvaluator evaluador
    ) {
        for (int columna = 0; columna < COLUMNAS; columna++) {
            if (!texto(
                    fila, columna, formatter, evaluador
            ).isBlank()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Obtiene el valor visible de una celda como texto.
     * Conserva códigos y documentos cuando Excel los almacena como texto.
     */
    private String texto(
            Row fila,
            int columna,
            DataFormatter formatter,
            FormulaEvaluator evaluador
    ) {
        Cell celda = fila.getCell(columna);

        return celda == null
                ? ""
                : formatter.formatCellValue(
                celda, evaluador
        ).trim();
    }

    /**
     * Convierte los importes a BigDecimal.
     * Las celdas vacías equivalen a cero.
     */
    private BigDecimal importe(
            Row fila,
            int columna,
            String nombre,
            int numeroFila,
            DataFormatter formatter,
            FormulaEvaluator evaluador,
            List<ComprobanteImportacionResponseDTO.ErrorValidacion> errores
    ) {
        Cell celda = fila.getCell(columna);

        if (celda == null
                || celda.getCellType() == CellType.BLANK) {
            return BigDecimal.ZERO;
        }

        try {
            if (celda.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(
                        celda.getNumericCellValue()
                );
            }

            String valor = texto(
                    fila, columna, formatter, evaluador
            );

            if (valor.isBlank()) {
                return BigDecimal.ZERO;
            }

            valor = valor.replace(" ", "")
                    .replace("$", "");

            if (valor.contains(",")
                    && valor.contains(".")) {
                if (valor.lastIndexOf(',')
                        > valor.lastIndexOf('.')) {
                    valor = valor.replace(".", "")
                            .replace(',', '.');
                } else {
                    valor = valor.replace(",", "");
                }
            } else if (valor.contains(",")) {
                valor = valor.replace(',', '.');
            }

            return new BigDecimal(valor);

        } catch (RuntimeException ex) {
            error(
                    errores, numeroFila, nombre,
                    "El importe no es numérico."
            );
            return null;
        }
    }

    /**
     * Registra un error de validación para mostrarlo en la vista previa.
     */
    private void error(
            List<ComprobanteImportacionResponseDTO.ErrorValidacion> errores,
            int fila,
            String columna,
            String mensaje
    ) {
        errores.add(
                new ComprobanteImportacionResponseDTO.ErrorValidacion(
                        fila, columna, mensaje
                )
        );
    }
}

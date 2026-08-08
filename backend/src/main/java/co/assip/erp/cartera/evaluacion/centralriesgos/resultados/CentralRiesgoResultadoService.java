package co.assip.erp.cartera.evaluacion.centralriesgos.resultados;

import co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto.CentralRiesgoResultadoDatoDTO;
import co.assip.erp.cartera.evaluacion.centralriesgos.resultados.dto.CentralRiesgoResultadoImportacionDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CentralRiesgoResultadoService {

    private static final String ENCABEZADO_ESPERADO =
            "documento;clasificacion_cartera;calificacion_guia_final;alertas_totales";

    private final CentralRiesgoResultadoRepository repository;

    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // IMPORTAR
    // =========================================================

    // =========================================================
// IMPORTAR
// =========================================================

    public CentralRiesgoResultadoImportacionDTO importar(
            Integer idCentralRiesgo,
            LocalDate fechaCorte,
            MultipartFile archivo,
            boolean reemplazar
    ) {

        validarSolicitud(
                idCentralRiesgo,
                fechaCorte,
                archivo
        );

        Optional<Integer> idArchivoAnterior =
                repository.buscarIdArchivo(
                        idCentralRiesgo,
                        fechaCorte
                );

        /*
         * Primera llamada:
         * si ya existen datos, no elimina ni importa.
         * Devuelve la información necesaria para que el frontend
         * solicite confirmación al usuario.
         */
        if (
                idArchivoAnterior.isPresent()
                        && !reemplazar
        ) {

            CentralRiesgoResultadoImportacionDTO respuesta =
                    repository.buscarArchivoPorId(
                                    idArchivoAnterior.get()
                            )
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "Existe una importación anterior, pero no fue posible consultarla."
                                    )
                            );

            respuesta.setRequiereConfirmacion(true);

            respuesta.setMensajeConfirmacion(
                    "Ya existe información importada para la Central de Riesgos "
                            + "y la fecha de corte seleccionada. "
                            + "Si continúa, los datos anteriores serán eliminados "
                            + "y reemplazados por el nuevo archivo."
            );

            return respuesta;
        }

        /*
         * El archivo nuevo se valida completamente antes de borrar
         * cualquier información anterior.
         */
        ResultadoLectura resultadoLectura =
                leerArchivo(
                        archivo
                );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        /*
         * Solo se elimina la carga anterior cuando:
         * 1. el usuario confirmó el reemplazo;
         * 2. el archivo nuevo pasó las validaciones.
         *
         * Como el Service es transaccional, si falla la nueva
         * inserción, PostgreSQL revierte también la eliminación.
         */
        if (
                idArchivoAnterior.isPresent()
                        && reemplazar
        ) {
            repository.eliminarArchivo(
                    idArchivoAnterior.get()
            );
        }

        String observaciones =
                construirObservaciones(
                        resultadoLectura
                );

        Integer idCentralArchivo =
                repository.crearArchivo(
                        idCentralRiesgo,
                        fechaCorte,
                        obtenerNombreArchivo(archivo),
                        archivo.getSize(),
                        resultadoLectura.cantidadLeidos(),
                        resultadoLectura.registros().size(),
                        resultadoLectura.cantidadRechazados(),
                        observaciones,
                        idUsuario
                );

        repository.insertarDatos(
                idCentralArchivo,
                resultadoLectura.registros(),
                idUsuario
        );

        CentralRiesgoResultadoImportacionDTO respuesta =
                repository.buscarArchivoPorId(
                                idCentralArchivo
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "La importación fue realizada, pero no fue posible consultar su resultado."
                                )
                        );

        respuesta.setRequiereConfirmacion(false);
        respuesta.setMensajeConfirmacion(null);

        return respuesta;
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    @Transactional(readOnly = true)
    public CentralRiesgoResultadoImportacionDTO buscarArchivoPorId(
            Integer idCentralArchivo
    ) {

        validarIdArchivo(
                idCentralArchivo
        );

        return repository.buscarArchivoPorId(
                        idCentralArchivo
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No existe la importación de Central de Riesgos solicitada."
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<CentralRiesgoResultadoDatoDTO> listarDatos(
            Integer idCentralArchivo
    ) {

        buscarArchivoPorId(
                idCentralArchivo
        );

        return repository.listarDatos(
                idCentralArchivo
        );
    }

    // =========================================================
    // LECTURA DEL CSV
    // =========================================================

    private ResultadoLectura leerArchivo(
            MultipartFile archivo
    ) {

        List<CentralRiesgoResultadoDatoDTO> registros =
                new ArrayList<>();

        Set<String> documentos =
                new HashSet<>();

        List<String> errores =
                new ArrayList<>();

        int cantidadLeidos = 0;
        int cantidadRechazados = 0;

        try (
                BufferedReader lector =
                        new BufferedReader(
                                new InputStreamReader(
                                        archivo.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String encabezado =
                    lector.readLine();

            validarEncabezado(
                    encabezado
            );

            String linea;
            int numeroFila = 1;

            while (
                    (linea = lector.readLine()) != null
            ) {

                numeroFila++;

                if (linea.isBlank()) {
                    continue;
                }

                cantidadLeidos++;

                String[] columnas =
                        linea.split(
                                ";",
                                -1
                        );

                if (columnas.length != 4) {
                    cantidadRechazados++;

                    registrarError(
                            errores,
                            numeroFila,
                            "La fila no contiene exactamente 4 columnas."
                    );

                    continue;
                }

                String documento =
                        limpiar(
                                columnas[0]
                        );

                String clasificacionCartera =
                        limpiarMayuscula(
                                columnas[1]
                        );

                String calificacion =
                        limpiar(
                                columnas[2]
                        );

                String alertasTexto =
                        limpiar(
                                columnas[3]
                        );

                if (documento == null) {

                    cantidadRechazados++;

                    registrarError(
                            errores,
                            numeroFila,
                            "El documento está vacío."
                    );

                    continue;
                }

                if (clasificacionCartera == null) {
                    cantidadRechazados++;

                    registrarError(
                            errores,
                            numeroFila,
                            "La clasificación de cartera está vacía."
                    );

                    continue;
                }

                if (
                        !"C".equals(clasificacionCartera)
                                && !"M".equals(clasificacionCartera)
                                && !"P".equals(clasificacionCartera)
                                && !"S".equals(clasificacionCartera)
                ) {
                    cantidadRechazados++;

                    registrarError(
                            errores,
                            numeroFila,
                            "La clasificación de cartera debe ser C, M, P o S."
                    );

                    continue;
                }

                String llaveDuplicado =
                        documento
                                + "|"
                                + clasificacionCartera;

                if (!documentos.add(llaveDuplicado)) {
                    cantidadRechazados++;

                    registrarError(
                            errores,
                            numeroFila,
                            "La combinación documento y clasificación está duplicada en el archivo."
                    );

                    continue;
                }

                Integer alertasTotales;

                try {
                    alertasTotales =
                            convertirAlertas(
                                    alertasTexto
                            );
                } catch (IllegalArgumentException ex) {

                    cantidadRechazados++;

                    registrarError(
                            errores,
                            numeroFila,
                            ex.getMessage()
                    );

                    continue;
                }

                CentralRiesgoResultadoDatoDTO registro =
                        new CentralRiesgoResultadoDatoDTO();

                registro.setNumeroFila(
                        numeroFila
                );

                registro.setDocumento(
                        documento
                );

                registro.setClasificacionCartera(
                        clasificacionCartera
                );

                registro.setCalificacionGuiaFinal(
                        calificacion
                );

                registro.setAlertasTotales(
                        alertasTotales
                );

                registros.add(
                        registro
                );
            }

        } catch (IOException ex) {

            throw new IllegalArgumentException(
                    "No fue posible leer el archivo CSV.",
                    ex
            );
        }

        if (cantidadLeidos == 0) {
            throw new IllegalArgumentException(
                    "El archivo CSV no contiene registros para importar."
            );
        }

        return new ResultadoLectura(
                registros,
                cantidadLeidos,
                cantidadRechazados,
                errores
        );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            Integer idCentralRiesgo,
            LocalDate fechaCorte,
            MultipartFile archivo
    ) {

        if (
                idCentralRiesgo == null
                        || idCentralRiesgo <= 0
        ) {
            throw new IllegalArgumentException(
                    "La Central de Riesgos es obligatoria."
            );
        }

        if (
                !repository.existeCentralActiva(
                        idCentralRiesgo
                )
        ) {
            throw new IllegalArgumentException(
                    "La Central de Riesgos seleccionada no existe o está inactiva."
            );
        }

        if (fechaCorte == null) {
            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }

        LocalDate ultimoDiaMes =
                fechaCorte.withDayOfMonth(
                        fechaCorte.lengthOfMonth()
                );

        if (!fechaCorte.equals(ultimoDiaMes)) {
            throw new IllegalArgumentException(
                    "La fecha de corte debe corresponder al último día del mes."
            );
        }

        if (
                archivo == null
                        || archivo.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "El archivo CSV es obligatorio."
            );
        }

        String nombreArchivo =
                obtenerNombreArchivo(
                        archivo
                );

        if (
                !nombreArchivo
                        .toLowerCase(Locale.ROOT)
                        .endsWith(".csv")
        ) {
            throw new IllegalArgumentException(
                    "El archivo debe tener extensión CSV."
            );
        }
    }

    private void validarEncabezado(
            String encabezado
    ) {

        if (encabezado == null) {
            throw new IllegalArgumentException(
                    "El archivo CSV no contiene encabezado."
            );
        }

        String encabezadoNormalizado =
                quitarBom(
                        encabezado
                )
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (
                !ENCABEZADO_ESPERADO.equals(
                        encabezadoNormalizado
                )
        ) {
            throw new IllegalArgumentException(
                    "El encabezado del archivo no corresponde al formato esperado: "
                            + ENCABEZADO_ESPERADO
            );
        }
    }

    private void validarIdArchivo(
            Integer idCentralArchivo
    ) {

        if (
                idCentralArchivo == null
                        || idCentralArchivo <= 0
        ) {
            throw new IllegalArgumentException(
                    "El identificador de la importación no es válido."
            );
        }
    }

    // =========================================================
    // CONVERSIONES
    // =========================================================

    private Integer convertirAlertas(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        try {

            int resultado =
                    Integer.parseInt(
                            valor
                    );

            if (resultado < 0) {
                throw new IllegalArgumentException(
                        "El total de alertas no puede ser negativo."
                );
            }

            return resultado;

        } catch (NumberFormatException ex) {

            throw new IllegalArgumentException(
                    "El total de alertas debe ser un número entero o venir vacío."
            );
        }
    }

    private String obtenerNombreArchivo(
            MultipartFile archivo
    ) {

        String nombre =
                archivo.getOriginalFilename();

        if (
                nombre == null
                        || nombre.isBlank()
        ) {
            return "central_riesgos.csv";
        }

        return nombre.trim();
    }

    private String limpiar(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String resultado =
                valor.trim();

        return resultado.isEmpty()
                ? null
                : resultado;
    }

    private String quitarBom(
            String valor
    ) {

        if (
                valor != null
                        && valor.startsWith("\uFEFF")
        ) {
            return valor.substring(1);
        }

        return valor;
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    private void registrarError(
            List<String> errores,
            int numeroFila,
            String mensaje
    ) {

        if (errores.size() >= 20) {
            return;
        }

        errores.add(
                "Fila "
                        + numeroFila
                        + ": "
                        + mensaje
        );
    }

    private String construirObservaciones(
            ResultadoLectura resultado
    ) {

        if (resultado.errores().isEmpty()) {
            return null;
        }

        String texto =
                String.join(
                        " | ",
                        resultado.errores()
                );

        if (texto.length() > 1000) {
            return texto.substring(
                    0,
                    1000
            );
        }

        return texto;
    }

    // =========================================================
    // RESULTADO INTERNO
    // =========================================================

    private record ResultadoLectura(
            List<CentralRiesgoResultadoDatoDTO> registros,
            int cantidadLeidos,
            int cantidadRechazados,
            List<String> errores
    ) {
    }

    private String limpiarMayuscula(
            String valor
    ) {

        String resultado =
                limpiar(valor);

        return resultado == null
                ? null
                : resultado.toUpperCase();
    }
}
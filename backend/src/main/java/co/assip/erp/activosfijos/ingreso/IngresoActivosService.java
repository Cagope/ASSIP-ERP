package co.assip.erp.activosfijos.ingreso;

import co.assip.erp.activosfijos.ingreso.dto.*;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngresoActivosService {

    private final IngresoActivosRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    @Transactional
    public IngresoActivosResponseDTO ingresarActivos(IngresoActivosRequestDTO req) {

        // =========================================================
        // AGENCIA DESDE TOKEN
        // =========================================================

        IngresoActivoHeaderDTO h = req.getHeader();
        if (h == null) {
            throw new IllegalArgumentException("El header es obligatorio.");
        }

        Long idAgencia = h.getIdAgencia();
        if (idAgencia == null) {
            throw new IllegalArgumentException("idAgencia es obligatoria en el header.");
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =========================================================
        // HEADER: VALIDACIONES
        // =========================================================
        if (h.getFechaInclusion() == null) {
            throw new IllegalArgumentException("fechaInclusion es obligatoria.");
        }

        LocalDate fecha = h.getFechaInclusion();
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("fechaInclusion no puede ser futura.");
        }

        // 🔒 VALIDAR AÑO Y MES CONTABLE (por agencia)
        repository.validarAnoAbierto(idAgencia, fecha.getYear());
        repository.validarMesAbierto(idAgencia, fecha.getYear(), fecha.getMonthValue());

        String tipoComp = safeTrim(h.getTipoComprobante());
        if (tipoComp.isBlank()) {
            throw new IllegalArgumentException("tipoComprobante es obligatorio.");
        }

        // =========================================================
        // NUMERO COMPROBANTE (7 dígitos)
        // - Si viene vacío → lo generamos desde tipos_comprobantes (consecutivo)
        // - Si viene → lo normalizamos (solo números + pad a 10)
        // =========================================================
        String numero10;
        if (isBlankDigits(h.getNumeroComprobante())) {
            Integer next = repository.obtenerYActualizarConsecutivo(idAgencia, tipoComp);
            numero10 = String.format("%07d", next);
        } else {
            numero10 = normalizeNumeroComprobante10(h.getNumeroComprobante());
        }

        String detalle = safeTrim(h.getDetalle());
        if (detalle.isBlank()) {
            throw new IllegalArgumentException("detalle es obligatorio.");
        }
        if (detalle.length() > 100) {
            throw new IllegalArgumentException("detalle máximo 100 caracteres.");
        }

        // =========================================================
        // EXISTE COMPROBANTE (por agencia + tipo + numero)
        // (según tu decisión: se valida en auxiliares_contables)
        // =========================================================
        if (repository.existeComprobante(idAgencia, tipoComp, numero10)) {
            throw new IllegalArgumentException("El comprobante ya existe (tipo + número + agencia).");
        }

        // Catálogos base del header (por agencia)
        repository.validarProveedorActivoEnAgencia(idAgencia, h.getIdProveedor());
        repository.validarCuentaActivaEnAgencia(idAgencia, h.getIdCuentaFactura());

        // =========================================================
        // ITEMS
        // =========================================================
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos 1 item.");
        }

        List<Long> idsActivos = new ArrayList<>();
        BigDecimal totalDebito  = BigDecimal.ZERO;
        BigDecimal totalCredito = BigDecimal.ZERO;

        // =========================================================
        // 1) HEADER CONTABLE → conceptos_contables
        // =========================================================
        repository.insertarConceptoContable(
                tipoComp,
                numero10,
                detalle,
                idUsuario
        );

        // =========================================================
        // 2) ITEMS → AUXILIARES + ACTIVOS
        // =========================================================
        for (IngresoActivoItemDTO it : req.getItems()) {

            String placa  = safeTrim(it.getPlacaActivo());
            String nombre = safeTrim(it.getNombreActivo());

            if (placa.isBlank()) throw new IllegalArgumentException("placaActivo es obligatoria.");
            if (nombre.isBlank()) throw new IllegalArgumentException("nombreActivo es obligatorio.");

            if (repository.existePlacaEnAgencia(idAgencia, placa)) {
                throw new IllegalArgumentException("La placa ya existe en la agencia: " + placa);
            }

            // ---- Agencia-scope
            repository.validarUbicacionEnAgencia(idAgencia, it.getIdUbicacion());

            // ---- Catálogos
            repository.validarBloqueActivo(it.getIdBloque());
            repository.validarFormaDepreciacionActiva(it.getIdFormaDepreciacion());
            repository.validarTipoAdquisicionActivo(it.getIdTipoAdquisicion());
            repository.validarEstadoActivo(it.getIdEstadoActivo());

            // ---- Cuentas (todas en agencia)
            repository.validarCuentaActivaEnAgencia(idAgencia, it.getIdCuentaActivo());
            repository.validarCuentaActivaEnAgencia(idAgencia, it.getIdCuentaDepreciacion());
            repository.validarCuentaActivaEnAgencia(idAgencia, it.getIdCuentaGasto());
            repository.validarCuentaActivaEnAgencia(idAgencia, it.getIdCuentaControl());

            if (it.getIdCuentaIvaActivo() != null) {
                repository.validarCuentaActivaEnAgencia(idAgencia, it.getIdCuentaIvaActivo());
            }

            // ---- Proveedor por ítem (si viene)
            if (it.getIdProveedor() != null) {
                repository.validarProveedorActivoEnAgencia(idAgencia, it.getIdProveedor());
            }

            BigDecimal vh  = nz(it.getValorHistorico());
            BigDecimal iva = nz(it.getValorIva());
            BigDecimal ret = nz(it.getValorRetencion());
            Integer meses  = it.getMesesDepreciacion();

            if (meses == null || meses <= 0) {
                throw new IllegalArgumentException("mesesDepreciacion debe ser mayor a 0.");
            }

            if (ret.compareTo(BigDecimal.ZERO) > 0) {
                if (it.getIdCuentaRetencion() == null) {
                    throw new IllegalArgumentException("Si hay retención, debe enviar idCuentaRetencion.");
                }
                repository.validarCuentaActivaEnAgencia(idAgencia, it.getIdCuentaRetencion());
            }

            BigDecimal valorBase = vh.add(iva);
            BigDecimal valorMensual = valorBase.divide(
                    BigDecimal.valueOf(meses),
                    2,
                    RoundingMode.HALF_UP
            );

            // =====================================================
            // CONTABILIDAD → auxiliares_contables
            // =====================================================
            // Débito activo
            repository.insertarAuxiliar(
                    idAgencia,
                    it.getIdCuentaActivo(),
                    fecha,
                    tipoComp,
                    numero10,
                    vh,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    detalle,
                    idUsuario
            );

            // Débito IVA (si hay)
            if (iva.compareTo(BigDecimal.ZERO) > 0 && it.getIdCuentaIvaActivo() != null) {
                repository.insertarAuxiliar(
                        idAgencia,
                        it.getIdCuentaIvaActivo(),
                        fecha,
                        tipoComp,
                        numero10,
                        iva,
                        BigDecimal.ZERO,
                        vh,
                        detalle,
                        idUsuario
                );
            }

            // Crédito retención (si hay)
            if (ret.compareTo(BigDecimal.ZERO) > 0) {
                repository.insertarAuxiliar(
                        idAgencia,
                        it.getIdCuentaRetencion(),
                        fecha,
                        tipoComp,
                        numero10,
                        BigDecimal.ZERO,
                        ret,
                        BigDecimal.ZERO,
                        detalle,
                        idUsuario
                );
            }

            totalDebito  = totalDebito.add(vh).add(iva);
            totalCredito = totalCredito.add(ret);

            // =====================================================
            // ACTIVO FIJO
            // =====================================================
            Long proveedorFinal =
                    (it.getIdProveedor() != null)
                            ? it.getIdProveedor()
                            : h.getIdProveedor();

            Long idActivo = repository.insertarActivoFijo(
                    idAgencia,
                    idUsuario,
                    placa,
                    nombre,
                    fecha,
                    it.getFechaGarantia(),
                    it.getIdFormaDepreciacion(),
                    meses,
                    valorBase,
                    valorMensual,
                    it.getIdEstadoActivo(),
                    it.getIdTipoAdquisicion(),
                    it.getIdUbicacion(),
                    it.getIdBloque(),
                    it.getIdResponsable(),
                    proveedorFinal,
                    it.getIdCuentaActivo(),
                    it.getIdCuentaDepreciacion(),
                    it.getIdCuentaGasto(),
                    it.getIdCuentaControl()
            );

            // Si tienes puente real, aquí lo usas.
            // Si no existe, queda NO-OP en repo (como ya lo tienes).
            repository.vincularActivoAComprobante(idActivo, null);

            idsActivos.add(idActivo);
        }

        // =========================================================
        // CONTRAPARTIDA FACTURA (Crédito) → auxiliares_contables
        // =========================================================
        BigDecimal neto = totalDebito.subtract(totalCredito);
        if (neto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El neto del comprobante debe ser mayor a 0.");
        }

        repository.insertarAuxiliar(
                idAgencia,
                h.getIdCuentaFactura(),
                fecha,
                tipoComp,
                numero10,
                BigDecimal.ZERO,
                neto,
                BigDecimal.ZERO,
                detalle,
                idUsuario
        );

        totalCredito = totalCredito.add(neto);

        if (totalDebito.compareTo(totalCredito) != 0) {
            throw new IllegalStateException("El comprobante no cuadra: débito != crédito.");
        }

        // =========================================================
        // RESPONSE
        // =========================================================
        IngresoActivosResponseDTO resp = new IngresoActivosResponseDTO();
        resp.setIdComprobante(null); // tu modelo contable no maneja id_comprobante
        resp.setTipoComprobante(tipoComp);
        resp.setNumeroComprobante(numero10);
        resp.setFechaInclusion(fecha);
        resp.setCantidadActivos(idsActivos.size());
        resp.setTotalDebito(totalDebito);
        resp.setTotalCredito(totalCredito);
        resp.setIdsActivosCreados(idsActivos);

        return resp;
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private static BigDecimal nz(BigDecimal x) {
        return (x == null) ? BigDecimal.ZERO : x;
    }

    private static boolean isBlankDigits(String raw) {
        if (raw == null) return true;
        String digits = raw.replaceAll("\\D", "");
        return digits.isBlank();
    }

    private static String normalizeNumeroComprobante10(String raw) {
        String digits = (raw == null) ? "" : raw.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("numeroComprobante es obligatorio.");
        }
        if (digits.length() > 10) {
            throw new IllegalArgumentException("numeroComprobante máximo 7 dígitos.");
        }
        return String.format("%07d", Long.parseLong(digits));
    }
}

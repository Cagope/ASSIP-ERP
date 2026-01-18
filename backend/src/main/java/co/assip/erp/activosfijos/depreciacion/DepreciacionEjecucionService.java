package co.assip.erp.activosfijos.depreciacion;

import co.assip.erp.contabilidad.registro.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.registro.dto.OrigenComprobanteDTO;
import co.assip.erp.contabilidad.registro.service.ContabilidadRegistroService;
import co.assip.erp.seguridad.utils.SecurityUtils;
import co.assip.erp.shared.config.EmpresaConfigService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DepreciacionEjecucionService {

    private final NamedParameterJdbcTemplate jdbc;
    private final DepreciacionPreviewRepository previewRepository;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final EmpresaConfigService empresaConfigService;

    public DepreciacionEjecucionService(
            NamedParameterJdbcTemplate jdbc,
            DepreciacionPreviewRepository previewRepository,
            ContabilidadRegistroService contabilidadRegistroService,
            EmpresaConfigService empresaConfigService
    ) {
        this.jdbc = jdbc;
        this.previewRepository = previewRepository;
        this.contabilidadRegistroService = contabilidadRegistroService;
        this.empresaConfigService = empresaConfigService;
    }

    /**
     * Ejecuta definitivamente la depreciación del período.
     * ✅ Graba extracto_activos (SOLO movimiento 71 = Depreciaciones)
     * ✅ Graba 1 comprobante contable GLOBAL del proceso (no por activo)
     * ✅ Actualiza activos_fijos (acumulado + neto + fecha última)
     * ✅ Incrementa CSC en contabilidad.tipos_comprobantes.csc_comprobante
     */
    @Transactional
    public DepreciacionEjecucionResult ejecutar(DepreciacionRequestDTO request) {

        // =========================================================
        // 🔐 Usuario autenticado (PATRÓN OFICIAL ASSIP)
        // =========================================================
        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }

        // =========================================================
        // 🏢 Tercero contable de la empresa (configurado en general.empresas)
        // =========================================================
        Integer idTerceroEmpresa = empresaConfigService.getIdTerceroEmpresa();

        // =========================================================
        // 0️⃣ Validaciones obligatorias
        // =========================================================
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        if (request.getIdAgencia() == null) {
            throw new IllegalArgumentException("La agencia es obligatoria");
        }

        if (request.getFechaPeriodo() == null) {
            throw new IllegalArgumentException("La fecha del período es obligatoria");
        }

        if (request.getFechaContabilizacion() == null) {
            throw new IllegalArgumentException("La fecha de contabilización es obligatoria");
        }

        if (request.getTipoComprobante() == null || request.getTipoComprobante().isBlank()) {
            throw new IllegalArgumentException("El tipo de comprobante es obligatorio");
        }

        if (request.getNumeroComprobante() == null || request.getNumeroComprobante().isBlank()) {
            throw new IllegalArgumentException("El número de comprobante es obligatorio");
        }

        if (request.getConcepto() == null || request.getConcepto().isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }

        // ✅ Número único para todo el proceso (NO se incrementa por activo)
        final String numeroComprobante = request.getNumeroComprobante();

        // =========================================================
        // 1️⃣ Validar período NO ejecutado (POR AGENCIA)
        // =========================================================
        String sqlExiste = """
            SELECT COUNT(1)
            FROM activos_fijos.depreciacion_activos_control
            WHERE fecha_periodo = :fechaPeriodo
              AND id_agencia = :idAgencia
              AND estado_proceso = 'EJECUTADO'
            """;

        Integer existe = jdbc.queryForObject(
                sqlExiste,
                new MapSqlParameterSource()
                        .addValue("fechaPeriodo", request.getFechaPeriodo())
                        .addValue("idAgencia", request.getIdAgencia()),
                Integer.class
        );

        if (existe != null && existe > 0) {
            throw new IllegalStateException(
                    "El período ya fue depreciado para la agencia " + request.getIdAgencia()
            );
        }

        // =========================================================
        // 2️⃣ Obtener preview definitivo ✅ FILTRADO POR AGENCIA
        // =========================================================
        List<DepreciacionPreviewDTO> detalle =
                previewRepository.obtenerPreview(request.getIdAgencia());

        if (detalle.isEmpty()) {
            throw new IllegalStateException(
                    "No existen activos para depreciar para la agencia " + request.getIdAgencia()
            );
        }

        // =========================================================
        // 3️⃣ Calcular totales (global del proceso)
        // =========================================================
        BigDecimal total = detalle.stream()
                .map(d -> d.getValorPeriodo() == null ? BigDecimal.ZERO : d.getValorPeriodo())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // =========================================================
        // 4️⃣ Insertar control del proceso ✅ CON AGENCIA
        // =========================================================
        String sqlControl = """
            INSERT INTO activos_fijos.depreciacion_activos_control (
                id_agencia,
                fecha_periodo,
                fecha_contabilizacion,
                tipo_comprobante,
                numero_comprobante,
                concepto,
                estado_proceso,
                fecha_ejecucion,
                total_debito,
                total_credito,
                fk_seguridad_creacion,
                fk_seguridad_actualizacion
            ) VALUES (
                :idAgencia,
                :fechaPeriodo,
                :fechaContabilizacion,
                :tipoComprobante,
                :numeroComprobante,
                :concepto,
                'EJECUTADO',
                now(),
                :total,
                :total,
                :usuario,
                :usuario
            )
            """;

        jdbc.update(sqlControl, new MapSqlParameterSource()
                .addValue("idAgencia", request.getIdAgencia())
                .addValue("fechaPeriodo", request.getFechaPeriodo())
                .addValue("fechaContabilizacion", request.getFechaContabilizacion())
                .addValue("tipoComprobante", request.getTipoComprobante())
                .addValue("numeroComprobante", numeroComprobante)
                .addValue("concepto", request.getConcepto())
                .addValue("total", total)
                .addValue("usuario", idUsuario)
        );

        // =========================================================
        // 5️⃣ Insert extracto_activos (SOLO movimiento 71 = Depreciaciones)
        // =========================================================
        String sqlExtracto = """
            INSERT INTO activos_fijos.extracto_activos (
                id_activo_fijo,
                fecha,
                hora,
                tipo_comprobante,
                numero_comprobante,
                codigo_movimiento,
                valor_debito,
                valor_credito,
                fk_seguridad_creacion,
                fk_seguridad_actualizacion
            ) VALUES (
                :idActivo,
                :fecha,
                :hora,
                :tipoComprobante,
                :numeroComprobante,
                :codigoMovimiento,
                :valorDebito,
                :valorCredito,
                :usuario,
                :usuario
            )
            """;

        // =========================================================
        // 6️⃣ Update activos_fijos (campos nuevos de depreciación + auditoría)
        // =========================================================
        String sqlUpdateActivo = """
            UPDATE activos_fijos.activos_fijos
            SET
              valor_depreciacion_acumulada = COALESCE(valor_depreciacion_acumulada, 0) + :valor,
              valor_neto = GREATEST(0, COALESCE(valor_neto, valor_adquisicion) - :valor),
              fecha_ultima_depreciacion = :fechaPeriodo,
              fk_seguridad_edicion = :usuario,
              fecha_edicion = now()
            WHERE id_activo_fijo = :idActivo
            """;

        // =========================================================
        // 7️⃣ Movimientos contables GLOBAL (2 por activo)
        // =========================================================
        List<MovimientoContableDTO> movimientosGlobal = new ArrayList<>();

        for (DepreciacionPreviewDTO d : detalle) {

            if (d.getValorPeriodo() == null || d.getValorPeriodo().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "El activo " + d.getPlacaActivo() + " tiene valor de depreciación inválido."
                );
            }

            if (d.getIdCuentaGasto() == null || d.getIdCuentaDepreciacion() == null) {
                throw new IllegalStateException(
                        "El activo " + d.getPlacaActivo() + " no tiene cuentas contables (gasto/depreciación)."
                );
            }

            String conceptoActivo = "Depreciación Activo [" + d.getPlacaActivo() + "] - " + request.getConcepto();

            // ✅ detalle_movimiento en auxiliares_contables es VARCHAR(100)
            if (conceptoActivo.length() > 100) {
                conceptoActivo = conceptoActivo.substring(0, 100);
            }

            // ✅ misma hora para el movimiento del activo
            LocalTime horaMov = LocalTime.now();

            // =========================================================
            // 5A) Grabar extracto_activos (SOLO 71 = Depreciaciones)
            // =========================================================
            jdbc.update(sqlExtracto, new MapSqlParameterSource()
                    .addValue("idActivo", d.getIdActivoFijo())
                    .addValue("fecha", request.getFechaContabilizacion())
                    .addValue("hora", horaMov)
                    .addValue("tipoComprobante", request.getTipoComprobante())
                    .addValue("numeroComprobante", numeroComprobante)
                    .addValue("codigoMovimiento", "71")
                    .addValue("valorDebito", BigDecimal.ZERO)
                    .addValue("valorCredito", d.getValorPeriodo())
                    .addValue("usuario", idUsuario)
            );

            // =========================
            // Movimientos contables (2 por activo)
            // =========================

            // Débito (Gasto)
            MovimientoContableDTO debito = new MovimientoContableDTO();
            debito.setIdCatalogoCuenta(d.getIdCuentaGasto().intValue());
            debito.setIdAgencia(request.getIdAgencia());
            debito.setIdDatosPersonal(idTerceroEmpresa); // ✅ tercero contable = empresa
            debito.setFechaAuxiliar(request.getFechaContabilizacion());
            debito.setTipoComprobante(request.getTipoComprobante());
            debito.setNumeroComprobante(numeroComprobante);
            debito.setDetalleMovimiento(conceptoActivo);
            debito.setValorDebito(d.getValorPeriodo());
            debito.setValorCredito(BigDecimal.ZERO);

            // Crédito (Depreciación acumulada)
            MovimientoContableDTO credito = new MovimientoContableDTO();
            credito.setIdCatalogoCuenta(d.getIdCuentaDepreciacion().intValue());
            credito.setIdAgencia(request.getIdAgencia());
            credito.setIdDatosPersonal(idTerceroEmpresa); // ✅ tercero contable = empresa
            credito.setFechaAuxiliar(request.getFechaContabilizacion());
            credito.setTipoComprobante(request.getTipoComprobante());
            credito.setNumeroComprobante(numeroComprobante);
            credito.setDetalleMovimiento(conceptoActivo);
            credito.setValorDebito(BigDecimal.ZERO);
            credito.setValorCredito(d.getValorPeriodo());

            movimientosGlobal.add(debito);
            movimientosGlobal.add(credito);

            // =========================================================
            // 6B) Actualizar activo (acumulado + neto + fecha última + auditoría)
            // =========================================================
            jdbc.update(sqlUpdateActivo, new MapSqlParameterSource()
                    .addValue("idActivo", d.getIdActivoFijo())
                    .addValue("valor", d.getValorPeriodo())
                    .addValue("fechaPeriodo", request.getFechaPeriodo())
                    .addValue("usuario", idUsuario)
            );
        }

        // =========================================================
        // ✅ Registrar 1 comprobante global del proceso
        // =========================================================
        OrigenComprobanteDTO origenGlobal = new OrigenComprobanteDTO();
        origenGlobal.setOrigenTipo("AUTO");
        origenGlobal.setModuloOrigen("ACTIVOS_FIJOS");
        origenGlobal.setProcesoOrigen("DEPRECIACION");
        origenGlobal.setTablaOrigen("activos_fijos.depreciacion_activos_control");
        origenGlobal.setIdOrigen(0L);

        contabilidadRegistroService.registrarComprobante(
                request.getIdAgencia(),
                request.getTipoComprobante(),
                numeroComprobante,
                request.getConcepto(),
                origenGlobal,
                movimientosGlobal,
                idUsuario
        );

        // =========================================================
        // ✅ Incrementar consecutivo del tipo comprobante (CSC)
        // =========================================================
        String sqlIncCsc = """
        UPDATE contabilidad.tipos_comprobantes
        SET csc_comprobante = csc_comprobante + 1
        WHERE tipo_comprobante = :tipoComprobante
          AND id_agencia = :idAgencia
        """;

        jdbc.update(sqlIncCsc, new MapSqlParameterSource()
                .addValue("tipoComprobante", request.getTipoComprobante())
                .addValue("idAgencia", request.getIdAgencia())
        );

        // =========================================================
        // 8️⃣ Resultado
        // =========================================================
        DepreciacionEjecucionResult result = new DepreciacionEjecucionResult();
        result.setActivosProcesados(detalle.size());
        result.setTotalDebito(total);
        result.setTotalCredito(total);

        return result;
    }
}

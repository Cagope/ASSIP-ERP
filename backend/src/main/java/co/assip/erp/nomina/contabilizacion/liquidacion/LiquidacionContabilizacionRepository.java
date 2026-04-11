package co.assip.erp.nomina.contabilizacion.liquidacion;

import co.assip.erp.nomina.contabilizacion.liquidacion.dto.LiquidacionMovimientoContableDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class LiquidacionContabilizacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // PREVIEW CONTABLE POR PERIODO
    // =========================================================
    public List<LiquidacionMovimientoContableDTO> preview(Integer idPeriodoNomina) {
        String sql = buildPreviewSql(false);
        MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("idPeriodoNomina", idPeriodoNomina);

        return jdbc.query(sql, ps, (rs, rowNum) ->
                LiquidacionMovimientoContableDTO.builder()
                        .idAgencia(rs.getInt("id_agencia"))
                        .idCatalogoCuenta(rs.getInt("id_catalogo_cuenta"))
                        .codigoCuenta(rs.getString("codigo_cuenta"))
                        .nombreCuenta(rs.getString("nombre_cuenta"))

                        .idTercero((Integer) rs.getObject("id_tercero"))
                        .documentoTercero(rs.getString("documento_tercero"))
                        .nombreTercero(rs.getString("nombre_tercero"))

                        .idEmpleadoReferencia((Integer) rs.getObject("id_empleado_referencia"))
                        .nombreEmpleadoReferencia(rs.getString("nombre_empleado_referencia"))

                        .debito(rs.getBigDecimal("debito"))
                        .credito(rs.getBigDecimal("credito"))

                        .valorBase(rs.getBigDecimal("valor_base"))

                        .build()
        );
    }

    // =========================================================
    // PREVIEW CONTABLE POR PERIODO Y AGENCIA
    // =========================================================
    public List<LiquidacionMovimientoContableDTO> previewPorAgencia(Integer idPeriodoNomina, Integer idAgencia) {
        String sql = buildPreviewSql(true);
        MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("idPeriodoNomina", idPeriodoNomina)
                .addValue("idAgencia", idAgencia);

        return jdbc.query(sql, ps, (rs, rowNum) -> LiquidacionMovimientoContableDTO.builder()
                .idAgencia(rs.getInt("id_agencia"))
                .idCatalogoCuenta(rs.getInt("id_catalogo_cuenta"))
                .codigoCuenta(rs.getString("codigo_cuenta"))
                .nombreCuenta(rs.getString("nombre_cuenta"))
                .idTercero((Integer) rs.getObject("id_tercero"))
                .documentoTercero(rs.getString("documento_tercero"))
                .nombreTercero(rs.getString("nombre_tercero"))
                .idEmpleadoReferencia((Integer) rs.getObject("id_empleado_referencia"))
                .nombreEmpleadoReferencia(rs.getString("nombre_empleado_referencia"))
                .debito(rs.getBigDecimal("debito"))
                .credito(rs.getBigDecimal("credito"))
                .build());
    }

    // =========================================================
    // AGENCIAS CON MOVIMIENTOS
    // =========================================================
    public List<Integer> listarAgenciasConMovimientos(Integer idPeriodoNomina) {
        String sql = """
            SELECT DISTINCT e.id_agencia
            FROM nomina.liquidacion_detalle ld
            JOIN nomina.liquidaciones l
              ON l.id_liquidacion = ld.id_liquidacion
            JOIN nomina.empleado_contratos ec
              ON ec.id_contrato = l.id_contrato
            JOIN nomina.empleados e
              ON e.id_empleado = ec.id_empleado
            WHERE l.id_periodo_nomina = :idPeriodoNomina
            ORDER BY e.id_agencia
            """;

        return jdbc.queryForList(sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                Integer.class);
    }

    // =========================================================
    // VALIDAR PERIODO CERRADO
    // =========================================================
    public boolean periodoEstaCerrado(Integer idPeriodoNomina) {
        String sql = """
        SELECT COUNT(1)
        FROM nomina.periodos_nomina
        WHERE id_periodo = :idPeriodoNomina
          AND estado = 'CERRADO'
        """;

        Integer count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                Integer.class);

        return count != null && count > 0;
    }

    // =========================================================
    // FECHA COMPROBANTE
    // Ajusta este SQL si tu tabla periodos_nomina usa otro nombre
    // =========================================================
    public LocalDate obtenerFechaComprobante(Integer idPeriodoNomina) {

        String sql = """
        SELECT COALESCE(fecha_fin, CURRENT_DATE)
        FROM nomina.periodos_nomina
        WHERE id_periodo = :idPeriodoNomina
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                LocalDate.class
        );
    }

    // =========================================================
    // CONSECUTIVO NM ACTUAL (LOCK)
    // =========================================================
    public Integer obtenerConsecutivoActualConLock(String tipoComprobante, Integer idAgencia) {
        String sql = """
            SELECT csc_comprobante
            FROM contabilidad.tipos_comprobantes
            WHERE tipo_comprobante = :tipoComprobante
              AND id_agencia = :idAgencia
              AND comprobante_activo = TRUE
            FOR UPDATE
            """;

        return jdbc.queryForObject(sql,
                new MapSqlParameterSource()
                        .addValue("tipoComprobante", tipoComprobante)
                        .addValue("idAgencia", idAgencia),
                Integer.class);
    }

    // =========================================================
    // ACTUALIZAR CONSECUTIVO
    // =========================================================
    public void actualizarConsecutivo(String tipoComprobante,
                                      Integer idAgencia,
                                      Integer nuevoConsecutivo,
                                      Integer idUsuario) {
        String sql = """
        UPDATE contabilidad.tipos_comprobantes
           SET csc_comprobante = :nuevoConsecutivo,
               fk_seguridad_edicion = :idUsuario,
               fecha_edicion = CURRENT_TIMESTAMP
         WHERE tipo_comprobante = :tipoComprobante
           AND id_agencia = :idAgencia
        """;

        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("nuevoConsecutivo", nuevoConsecutivo)
                .addValue("tipoComprobante", tipoComprobante)
                .addValue("idAgencia", idAgencia)
                .addValue("idUsuario", idUsuario));
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    // =========================================================
    // SQL BASE PREVIEW
    // =========================================================
    private String buildPreviewSql(boolean filtrarAgencia) {
        String filtroAgencia = filtrarAgencia ? " AND e.id_agencia = :idAgencia " : "";

        return """
            WITH movimientos AS (

            -- ==========================
            -- DEVENGADOS
            -- ==========================
            SELECT
                e.id_agencia,
                ccc.id_cuenta_debito AS id_catalogo_cuenta,
                e.id_datos_personal AS id_tercero,
                e.id_datos_personal AS id_empleado_referencia,
                SUM(ld.valor_total) AS debito,
                0::numeric AS credito,
                SUM(ld.base_calculo) AS valor_base
            FROM nomina.liquidacion_detalle ld
            JOIN nomina.liquidaciones l
                ON l.id_liquidacion = ld.id_liquidacion
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = l.id_contrato
            JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
            JOIN nomina.concepto_cuentas_contables ccc
                ON ccc.codigo_concepto = ld.codigo_concepto
               AND ccc.id_agencia = e.id_agencia
            WHERE ld.tipo = 'DEVENGADO'
              AND l.id_periodo_nomina = :idPeriodoNomina
              %s
            GROUP BY
                e.id_agencia,
                ccc.id_cuenta_debito,
                e.id_datos_personal

            UNION ALL

            -- ==========================
            -- DEDUCCIONES
            -- tercero = entidad
            -- valor = por empleado
            -- ==========================
            SELECT
                e.id_agencia,
                ccc.id_cuenta_credito AS id_catalogo_cuenta,
                CASE
                    WHEN ld.codigo_concepto = 'SALUD_EMP'
                        THEN eps.id_datos_personal
                    WHEN ld.codigo_concepto = 'PENSION_EMP'
                        THEN afp.id_datos_personal
                    ELSE e.id_datos_personal
                END AS id_tercero,
                e.id_datos_personal AS id_empleado_referencia,
                0::numeric AS debito,
                SUM(ld.valor_total) AS credito,
                SUM(ld.base_calculo) AS valor_base
            FROM nomina.liquidacion_detalle ld
            JOIN nomina.liquidaciones l
                ON l.id_liquidacion = ld.id_liquidacion
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = l.id_contrato
            JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
            LEFT JOIN nomina.entidades_eps eps
                ON eps.id_eps = ec.id_eps
            LEFT JOIN nomina.entidades_afp afp
                ON afp.id_afp = ec.id_afp
            JOIN nomina.concepto_cuentas_contables ccc
                ON ccc.codigo_concepto = ld.codigo_concepto
               AND ccc.id_agencia = e.id_agencia
            WHERE ld.tipo = 'DEDUCCION'
              AND l.id_periodo_nomina = :idPeriodoNomina
              %s
            GROUP BY
                e.id_agencia,
                ccc.id_cuenta_credito,
                e.id_datos_personal,
                eps.id_datos_personal,
                afp.id_datos_personal,
                ld.codigo_concepto

            UNION ALL

            -- ==========================
            -- NETO NOMINA
            -- ==========================
            SELECT
                e.id_agencia,
                pcn.id_cuenta_neto_nomina AS id_catalogo_cuenta,
                e.id_datos_personal AS id_tercero,
                e.id_datos_personal AS id_empleado_referencia,
                0::numeric AS debito,
                SUM(
                    CASE
                        WHEN ld.tipo = 'DEVENGADO' THEN ld.valor_total
                        WHEN ld.tipo = 'DEDUCCION' THEN -ld.valor_total
                    END
                ) AS credito,
                0::numeric AS valor_base
            FROM nomina.liquidacion_detalle ld
            JOIN nomina.liquidaciones l
                ON l.id_liquidacion = ld.id_liquidacion
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = l.id_contrato
            JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
            JOIN nomina.parametros_contables_nomina pcn
                ON pcn.id_agencia = e.id_agencia
            WHERE l.id_periodo_nomina = :idPeriodoNomina
              %s
            GROUP BY
                e.id_agencia,
                pcn.id_cuenta_neto_nomina,
                e.id_datos_personal
            )

            SELECT
                m.id_agencia,
                m.id_catalogo_cuenta,
                c.codigo_cuenta,
                c.nombre_cuenta,
                m.id_tercero,
                dp.documento AS documento_tercero,
                CASE
                    WHEN dp.tipo_persona = '2' THEN COALESCE(dp.nombres, '')
                    ELSE TRIM(BOTH FROM CONCAT(
                        COALESCE(dp.nombres, ''),
                        CASE
                            WHEN dp.primer_apellido IS NOT NULL AND dp.primer_apellido <> '' THEN ' ' || dp.primer_apellido
                            ELSE ''
                        END,
                        CASE
                            WHEN dp.segundo_apellido IS NOT NULL AND dp.segundo_apellido <> '' THEN ' ' || dp.segundo_apellido
                            ELSE ''
                        END
                    ))
                END AS nombre_tercero,
                m.id_empleado_referencia,
                CASE
                    WHEN dpe.tipo_persona = '2' THEN COALESCE(dpe.nombres, '')
                    ELSE TRIM(BOTH FROM CONCAT(
                        COALESCE(dpe.nombres, ''),
                        CASE
                            WHEN dpe.primer_apellido IS NOT NULL AND dpe.primer_apellido <> '' THEN ' ' || dpe.primer_apellido
                            ELSE ''
                        END,
                        CASE
                            WHEN dpe.segundo_apellido IS NOT NULL AND dpe.segundo_apellido <> '' THEN ' ' || dpe.segundo_apellido
                            ELSE ''
                        END
                    ))
                END AS nombre_empleado_referencia,
                dpe.nombres AS nombre_empleado_referencia,
                m.debito,
                m.credito,
                m.valor_base
            FROM movimientos m
            LEFT JOIN contabilidad.catalogo_cuentas c
                ON c.id_catalogo_cuenta = m.id_catalogo_cuenta
            LEFT JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal = m.id_tercero
            LEFT JOIN hoja_vida.datos_personales dpe
                ON dpe.id_datos_personal = m.id_empleado_referencia
            ORDER BY
                m.id_agencia,
                c.codigo_cuenta,
                dpe.nombres,
                dp.nombres
            """.formatted(filtroAgencia, filtroAgencia, filtroAgencia);
    }

    public boolean comprobanteExiste(String tipoComprobante,
                                     String numeroComprobante,
                                     Integer idAgencia) {

        String sql = """
        SELECT COUNT(1)
        FROM contabilidad.conceptos_contables
        WHERE tipo_comprobante = :tipoComprobante
        AND numero_comprobante = :numeroComprobante
        AND id_agencia = :idAgencia
        """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("tipoComprobante", tipoComprobante)
                        .addValue("numeroComprobante", numeroComprobante)
                        .addValue("idAgencia", idAgencia),
                Integer.class
        );

        return count != null && count > 0;
    }

    public boolean periodoContableCerrado(Integer idAgencia, LocalDate fecha) {

        String sql = """
        SELECT COUNT(1)
        FROM contabilidad.meses_cerrados
        WHERE id_agencia = :idAgencia
          AND ano = :ano
          AND mes = :mes
          AND estado = 'C'
        """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia)
                        .addValue("ano", fecha.getYear())
                        .addValue("mes", fecha.getMonthValue()),
                Integer.class
        );

        return count != null && count > 0;
    }

    // =========================================================
    // MARCAR PERIODO COMO CONTABILIZADO
    // =========================================================
    public void marcarPeriodoComoContabilizado(Integer idPeriodo, Integer idUsuario) {

        String sql = """
        UPDATE nomina.periodos_nomina
           SET estado = 'CONTABILIZADO',
               fecha_contabiliza = CURRENT_TIMESTAMP,
               fk_seguridad_contabiliza = :idUsuario,
               fk_seguridad_edicion = :idUsuario,
               fecha_edicion = CURRENT_TIMESTAMP
         WHERE id_periodo = :idPeriodo
        """;

        jdbc.update(sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodo", idPeriodo)
                        .addValue("idUsuario", idUsuario));
    }

    // =========================================================
    // VALIDAR PERIODO CONTABILIZADO
    // =========================================================
    public boolean periodoEstaContabilizado(Integer idPeriodoNomina) {

        String sql = """
        SELECT COUNT(1)
        FROM nomina.periodos_nomina
        WHERE id_periodo = :idPeriodoNomina
          AND estado = 'CONTABILIZADO'
        """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodoNomina", idPeriodoNomina),
                Integer.class
        );

        return count != null && count > 0;
    }

    public List<String> listarCuentasNoOperablesEnPreview(Integer idPeriodoNomina, Integer idAgencia) {

        String sql = """
        WITH movimientos AS (

            SELECT e.id_agencia, ccc.id_cuenta_debito AS id_catalogo_cuenta
            FROM nomina.liquidacion_detalle ld
            JOIN nomina.liquidaciones l
                ON l.id_liquidacion = ld.id_liquidacion
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = l.id_contrato
            JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
            JOIN nomina.concepto_cuentas_contables ccc
                ON ccc.codigo_concepto = ld.codigo_concepto
               AND ccc.id_agencia = e.id_agencia
            WHERE ld.tipo = 'DEVENGADO'
              AND l.id_periodo_nomina = :idPeriodoNomina
              AND e.id_agencia = :idAgencia

            UNION

            SELECT e.id_agencia, ccc.id_cuenta_credito AS id_catalogo_cuenta
            FROM nomina.liquidacion_detalle ld
            JOIN nomina.liquidaciones l
                ON l.id_liquidacion = ld.id_liquidacion
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = l.id_contrato
            JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
            JOIN nomina.concepto_cuentas_contables ccc
                ON ccc.codigo_concepto = ld.codigo_concepto
               AND ccc.id_agencia = e.id_agencia
            WHERE ld.tipo = 'DEDUCCION'
              AND l.id_periodo_nomina = :idPeriodoNomina
              AND e.id_agencia = :idAgencia

            UNION

            SELECT e.id_agencia, pcn.id_cuenta_neto_nomina AS id_catalogo_cuenta
            FROM nomina.liquidacion_detalle ld
            JOIN nomina.liquidaciones l
                ON l.id_liquidacion = ld.id_liquidacion
            JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = l.id_contrato
            JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
            JOIN nomina.parametros_contables_nomina pcn
                ON pcn.id_agencia = e.id_agencia
            WHERE l.id_periodo_nomina = :idPeriodoNomina
              AND e.id_agencia = :idAgencia
        )
        SELECT DISTINCT
            c.codigo_cuenta || ' - ' || c.nombre_cuenta
        FROM movimientos m
        JOIN contabilidad.catalogo_cuentas c
          ON c.id_catalogo_cuenta = m.id_catalogo_cuenta
        WHERE COALESCE(c.cuenta_operable, false) = false
        ORDER BY 1
        """;

        return jdbc.queryForList(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodoNomina", idPeriodoNomina)
                        .addValue("idAgencia", idAgencia),
                String.class
        );
    }

    // =========================================================
    // BLOQUEAR PERIODO NOMINA
    // =========================================================
    public void bloquearPeriodoNomina(Integer idPeriodoNomina) {

        String sql = """
    SELECT id_periodo
    FROM nomina.periodos_nomina
    WHERE id_periodo = :idPeriodoNomina
    FOR UPDATE
    """;

        jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodoNomina", idPeriodoNomina),
                Integer.class
        );
    }

    public String obtenerNumeroComprobanteNomina(Integer idPeriodoNomina, Integer idAgencia) {

        String sql = """
    SELECT numero_comprobante
    FROM contabilidad.origen_comprobantes
    WHERE id_agencia = :idAgencia
      AND modulo_origen = 'NOMINA'
      AND proceso_origen = 'LIQUIDACION_NOMINA'
      AND tabla_origen = 'nomina.liquidaciones'
      AND id_origen = :idPeriodoNomina
    """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodoNomina", idPeriodoNomina)
                        .addValue("idAgencia", idAgencia),
                String.class
        );
    }

    public List<LiquidacionMovimientoContableDTO> obtenerMovimientosComprobante(
            String tipoComprobante,
            String numeroComprobante,
            Integer idAgencia
    ) {

        String sql = """
    SELECT
        id_catalogo_cuenta,
        id_datos_personal,
        valor_debito,
        valor_credito,
        valor_base
    FROM contabilidad.auxiliares_contables
    WHERE tipo_comprobante = :tipoComprobante
      AND numero_comprobante = :numeroComprobante
      AND id_agencia = :idAgencia
    """;

        return jdbc.query(sql,
                new MapSqlParameterSource()
                        .addValue("tipoComprobante", tipoComprobante)
                        .addValue("numeroComprobante", numeroComprobante)
                        .addValue("idAgencia", idAgencia),
                (rs, rowNum) -> LiquidacionMovimientoContableDTO.builder()
                        .idCatalogoCuenta(rs.getInt("id_catalogo_cuenta"))
                        .idTercero((Integer) rs.getObject("id_datos_personal"))
                        .debito(rs.getBigDecimal("valor_debito"))
                        .credito(rs.getBigDecimal("valor_credito"))
                        .valorBase(rs.getBigDecimal("valor_base"))
                        .build()
        );
    }

    public void marcarPeriodoComoCerrado(Integer idPeriodo, Integer idUsuario) {

        String sql = """
        UPDATE nomina.periodos_nomina
           SET estado = 'CERRADO',
               fecha_contabiliza = NULL,
               fk_seguridad_contabiliza = NULL,
               fk_seguridad_edicion = :idUsuario,
               fecha_edicion = CURRENT_TIMESTAMP
         WHERE id_periodo = :idPeriodo
        """;

        jdbc.update(sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodo", idPeriodo)
                        .addValue("idUsuario", idUsuario));
    }

    public String obtenerComprobanteExistente(Integer idPeriodoNomina) {

        String sql = """
    SELECT numero_comprobante
    FROM contabilidad.origen_comprobantes
    WHERE modulo_origen = 'NOMINA'
      AND proceso_origen = 'LIQUIDACION_NOMINA'
      AND tabla_origen = 'nomina.liquidaciones'
      AND id_origen = :idPeriodoNomina
      AND estado_origen = 'ACTIVO'
    LIMIT 1
    """;

        List<String> res = jdbc.queryForList(
                sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                String.class
        );

        return res.isEmpty() ? null : res.get(0);
    }

    // =========================================================
    // MARCAR ORIGEN COMO REVERSADO
    // =========================================================
    public void marcarOrigenComoReversado(Integer idPeriodoNomina, Integer idUsuario) {

        String sql = """
        UPDATE contabilidad.origen_comprobantes
           SET estado_origen = 'REVERSADO',
               fk_seguridad_edicion = :idUsuario,
               fecha_edicion = CURRENT_TIMESTAMP
         WHERE modulo_origen = 'NOMINA'
           AND proceso_origen = 'LIQUIDACION_NOMINA'
           AND tabla_origen = 'nomina.liquidaciones'
           AND id_origen = :idPeriodoNomina
           AND estado_origen = 'ACTIVO'
        """;

        jdbc.update(sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodoNomina", idPeriodoNomina)
                        .addValue("idUsuario", idUsuario));
    }

}
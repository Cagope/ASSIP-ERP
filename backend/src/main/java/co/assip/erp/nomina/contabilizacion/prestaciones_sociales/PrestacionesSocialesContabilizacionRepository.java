package co.assip.erp.nomina.contabilizacion.prestaciones_sociales;

import co.assip.erp.nomina.contabilizacion.liquidacion.dto.LiquidacionMovimientoContableDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PrestacionesSocialesContabilizacionRepository {

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
    // AGENCIAS CON MOVIMIENTOS
    // =========================================================
    public List<Integer> listarAgenciasConMovimientos(Integer idPeriodoNomina) {
        String sql = """
        WITH periodo_base AS (
            SELECT
                p.id_agencia,
                p.anio,
                p.mes
            FROM nomina.periodos_nomina p
            WHERE p.id_periodo = :idPeriodoNomina
        ),
        periodo_mes AS (
            SELECT
                p.id_agencia,
                MIN(p.fecha_inicio) AS fecha_inicio_mes,
                MAX(p.fecha_fin) AS fecha_fin_mes
            FROM nomina.periodos_nomina p
            JOIN periodo_base b
              ON b.id_agencia = p.id_agencia
             AND b.anio = p.anio
             AND b.mes = p.mes
            GROUP BY p.id_agencia
        )
        SELECT DISTINCT e.id_agencia
        FROM periodo_mes pm
        JOIN nomina.empleados e
          ON e.id_agencia = pm.id_agencia
        JOIN nomina.empleado_contratos ec
          ON ec.id_empleado = e.id_empleado
        WHERE COALESCE(ec.activo, TRUE) = TRUE
          AND ec.fecha_inicio <= pm.fecha_fin_mes
          AND (ec.fecha_fin IS NULL OR ec.fecha_fin >= pm.fecha_inicio_mes)
        ORDER BY e.id_agencia
        """;

        return jdbc.queryForList(
                sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                Integer.class
        );
    }

    // =========================================================
    // FECHA COMPROBANTE
    // =========================================================
    public LocalDate obtenerFechaComprobante(Integer idPeriodoNomina) {
        String sql = """
    WITH periodo_base AS (
        SELECT id_agencia, anio, mes
        FROM nomina.periodos_nomina
        WHERE id_periodo = :idPeriodoNomina
    )
    SELECT COALESCE(MAX(p.fecha_fin), CURRENT_DATE)
    FROM nomina.periodos_nomina p
    JOIN periodo_base b
      ON b.id_agencia = p.id_agencia
     AND b.anio = p.anio
     AND b.mes = p.mes
    """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                LocalDate.class
        );
    }

    // =========================================================
    // CONSECUTIVO ACTUAL (LOCK)
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

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("tipoComprobante", tipoComprobante)
                        .addValue("idAgencia", idAgencia),
                Integer.class
        );
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

    // =========================================================
    // SQL BASE PREVIEW
    // =========================================================
    private String buildPreviewSql(boolean filtrarAgencia) {
        String filtroAgencia = filtrarAgencia ? " AND e.id_agencia = :idAgencia " : "";

        return """
    WITH periodo_base AS (
        SELECT
            p.id_periodo,
            p.id_agencia,
            p.anio,
            p.mes
        FROM nomina.periodos_nomina p
        WHERE p.id_periodo = :idPeriodoNomina
    ),

    periodo_mes AS (
        SELECT
            p.id_agencia,
            p.anio,
            p.mes,
            MIN(p.fecha_inicio) AS fecha_inicio_mes,
            MAX(p.fecha_fin) AS fecha_fin_mes
        FROM nomina.periodos_nomina p
        JOIN periodo_base b
          ON b.id_agencia = p.id_agencia
         AND b.anio = p.anio
         AND b.mes = p.mes
        GROUP BY
            p.id_agencia,
            p.anio,
            p.mes
    ),

    variables AS (
        SELECT
            vv.por_provision_prima,
            vv.por_provision_prima_semestral,
            vv.por_provision_vacaciones,
            vv.por_provision_cesantias,
            vv.por_provision_interes_cesantias
        FROM nomina.variables_vigencia vv
        CROSS JOIN periodo_mes pm
        WHERE vv.activo = TRUE
          AND pm.fecha_fin_mes BETWEEN vv.fecha_inicial AND vv.fecha_final
        ORDER BY vv.fecha_inicial DESC
        LIMIT 1
    ),

    contratos_base AS (
        SELECT
            e.id_agencia,
            e.id_empleado,
            e.id_datos_personal,
            ec.id_contrato,
            COALESCE(ec.salario_base, 0)::numeric AS salario_base,
            COALESCE(ec.liquida_prima_semestral, FALSE) AS liquida_prima_semestral,
            COALESCE(tc.aplica_cesantias, FALSE) AS aplica_cesantias,
            COALESCE(tc.aplica_prima, FALSE) AS aplica_prima,
            COALESCE(tc.aplica_vacaciones, FALSE) AS aplica_vacaciones
        FROM periodo_mes pm
        JOIN nomina.empleados e
          ON e.id_agencia = pm.id_agencia
        JOIN nomina.empleado_contratos ec
          ON ec.id_empleado = e.id_empleado
        JOIN nomina.tipos_contrato tc
          ON tc.id_tipo_contrato = ec.id_tipo_contrato
        WHERE COALESCE(ec.activo, TRUE) = TRUE
          AND ec.fecha_inicio <= pm.fecha_fin_mes
          AND (ec.fecha_fin IS NULL OR ec.fecha_fin >= pm.fecha_inicio_mes)
          %s
    ),

    detalle_mes AS (
        SELECT
            e.id_agencia,
            l.id_contrato,
            e.id_datos_personal,
            ld.codigo_concepto,
            SUM(COALESCE(ld.valor_total, 0)) AS valor_total
        FROM nomina.liquidacion_detalle ld
        JOIN nomina.liquidaciones l
          ON l.id_liquidacion = ld.id_liquidacion
        JOIN nomina.empleado_contratos ec
          ON ec.id_contrato = l.id_contrato
        JOIN nomina.empleados e
          ON e.id_empleado = ec.id_empleado
        JOIN periodo_base b
          ON b.id_agencia = e.id_agencia
        JOIN nomina.periodos_nomina p
          ON p.id_periodo = l.id_periodo_nomina
         AND p.id_agencia = b.id_agencia
         AND p.anio = b.anio
         AND p.mes = b.mes
        GROUP BY
            e.id_agencia,
            l.id_contrato,
            e.id_datos_personal,
            ld.codigo_concepto
    ),

    base_cesantias AS (
        SELECT
            cb.id_agencia,
            cb.id_contrato,
            cb.id_datos_personal,
            COALESCE(SUM(dm.valor_total), 0)::numeric AS valor_base
        FROM contratos_base cb
        LEFT JOIN detalle_mes dm
          ON dm.id_agencia = cb.id_agencia
         AND dm.id_contrato = cb.id_contrato
        LEFT JOIN nomina.conceptos_nomina cn
          ON cn.codigo_concepto = dm.codigo_concepto
        WHERE COALESCE(cn.afecta_base_cesantias, FALSE) = TRUE
           OR dm.codigo_concepto IS NULL
        GROUP BY
            cb.id_agencia,
            cb.id_contrato,
            cb.id_datos_personal
    ),

    base_prima_legal AS (
        SELECT
            cb.id_agencia,
            cb.id_contrato,
            cb.id_datos_personal,
            COALESCE(SUM(dm.valor_total), 0)::numeric AS valor_base
        FROM contratos_base cb
        LEFT JOIN detalle_mes dm
          ON dm.id_agencia = cb.id_agencia
         AND dm.id_contrato = cb.id_contrato
        LEFT JOIN nomina.conceptos_nomina cn
          ON cn.codigo_concepto = dm.codigo_concepto
        WHERE COALESCE(cn.afecta_base_prima_legal, FALSE) = TRUE
           OR dm.codigo_concepto IS NULL
        GROUP BY
            cb.id_agencia,
            cb.id_contrato,
            cb.id_datos_personal
    ),

    base_vacaciones_var AS (
        SELECT
            cb.id_agencia,
            cb.id_contrato,
            cb.id_datos_personal,
            COALESCE(SUM(dm.valor_total), 0)::numeric AS valor_variable
        FROM contratos_base cb
        LEFT JOIN detalle_mes dm
          ON dm.id_agencia = cb.id_agencia
         AND dm.id_contrato = cb.id_contrato
        LEFT JOIN nomina.conceptos_nomina cn
          ON cn.codigo_concepto = dm.codigo_concepto
        WHERE COALESCE(cn.afecta_base_vacaciones, FALSE) = TRUE
           OR dm.codigo_concepto IS NULL
        GROUP BY
            cb.id_agencia,
            cb.id_contrato,
            cb.id_datos_personal
    ),

    base_vacaciones AS (
        SELECT
            cb.id_agencia,
            cb.id_contrato,
            cb.id_datos_personal,
            (COALESCE(cb.salario_base, 0) + (COALESCE(bv.valor_variable, 0) * 2))::numeric AS valor_base
        FROM contratos_base cb
        LEFT JOIN base_vacaciones_var bv
          ON bv.id_agencia = cb.id_agencia
         AND bv.id_contrato = cb.id_contrato
         AND bv.id_datos_personal = cb.id_datos_personal
    ),

    base_prima_semestral AS (
        SELECT
            cb.id_agencia,
            cb.id_contrato,
            cb.id_datos_personal,
            COALESCE(SUM(dm.valor_total), 0)::numeric AS valor_base
        FROM contratos_base cb
        LEFT JOIN detalle_mes dm
          ON dm.id_agencia = cb.id_agencia
         AND dm.id_contrato = cb.id_contrato
        LEFT JOIN nomina.conceptos_nomina cn
          ON cn.codigo_concepto = dm.codigo_concepto
        WHERE COALESCE(cn.afecta_base_prima_semestral, FALSE) = TRUE
           OR dm.codigo_concepto IS NULL
        GROUP BY
            cb.id_agencia,
            cb.id_contrato,
            cb.id_datos_personal
    ),

    conceptos_calculados AS (

        -- CESANTIAS
        SELECT
            cb.id_agencia,
            cb.id_datos_personal AS id_tercero,
            cb.id_datos_personal AS id_empleado_referencia,
            'PROV_CESANTIAS' AS codigo_concepto,
            ROUND(bc.valor_base * (v.por_provision_cesantias / 100.0), 0) AS valor,
            bc.valor_base AS valor_base
        FROM contratos_base cb
        JOIN base_cesantias bc
          ON bc.id_agencia = cb.id_agencia
         AND bc.id_contrato = cb.id_contrato
         AND bc.id_datos_personal = cb.id_datos_personal
        CROSS JOIN variables v
        WHERE cb.aplica_cesantias = TRUE

        UNION ALL

        -- INTERESES CESANTIAS
        SELECT
            cb.id_agencia,
            cb.id_datos_personal AS id_tercero,
            cb.id_datos_personal AS id_empleado_referencia,
            'PROV_INT_CES' AS codigo_concepto,
            ROUND(bc.valor_base * (v.por_provision_interes_cesantias / 100.0), 0) AS valor,
            bc.valor_base AS valor_base
        FROM contratos_base cb
        JOIN base_cesantias bc
          ON bc.id_agencia = cb.id_agencia
         AND bc.id_contrato = cb.id_contrato
         AND bc.id_datos_personal = cb.id_datos_personal
        CROSS JOIN variables v
        WHERE cb.aplica_cesantias = TRUE

        UNION ALL

        -- PRIMA LEGAL / SERVICIOS
        SELECT
            cb.id_agencia,
            cb.id_datos_personal AS id_tercero,
            cb.id_datos_personal AS id_empleado_referencia,
            'PRIMA_SERV' AS codigo_concepto,
            ROUND(bp.valor_base * (v.por_provision_prima / 100.0), 0) AS valor,
            bp.valor_base AS valor_base
        FROM contratos_base cb
        JOIN base_prima_legal bp
          ON bp.id_agencia = cb.id_agencia
         AND bp.id_contrato = cb.id_contrato
         AND bp.id_datos_personal = cb.id_datos_personal
        CROSS JOIN variables v
        WHERE cb.aplica_prima = TRUE

        UNION ALL

        -- VACACIONES
        SELECT
            cb.id_agencia,
            cb.id_datos_personal AS id_tercero,
            cb.id_datos_personal AS id_empleado_referencia,
            'PROV_VACACIONES' AS codigo_concepto,
            ROUND(bv.valor_base * (v.por_provision_vacaciones / 100.0), 0) AS valor,
            bv.valor_base AS valor_base
        FROM contratos_base cb
        JOIN base_vacaciones bv
          ON bv.id_agencia = cb.id_agencia
         AND bv.id_contrato = cb.id_contrato
         AND bv.id_datos_personal = cb.id_datos_personal
        CROSS JOIN variables v
        WHERE cb.aplica_vacaciones = TRUE

        UNION ALL

        -- PRIMA SEMESTRAL
        SELECT
            cb.id_agencia,
            cb.id_datos_personal AS id_tercero,
            cb.id_datos_personal AS id_empleado_referencia,
            'PRIMA_SEM' AS codigo_concepto,
            ROUND(bps.valor_base * (v.por_provision_prima_semestral / 100.0), 0) AS valor,
            bps.valor_base AS valor_base
        FROM contratos_base cb
        JOIN base_prima_semestral bps
          ON bps.id_agencia = cb.id_agencia
         AND bps.id_contrato = cb.id_contrato
         AND bps.id_datos_personal = cb.id_datos_personal
        CROSS JOIN variables v
        WHERE cb.aplica_prima = TRUE
          AND cb.liquida_prima_semestral = TRUE
    ),

    movimientos AS (

        SELECT
            cc.id_agencia,
            ccc.id_cuenta_debito AS id_catalogo_cuenta,
            cc.id_tercero,
            cc.id_empleado_referencia,
            cc.valor AS debito,
            0::numeric AS credito,
            cc.valor_base
        FROM conceptos_calculados cc
        JOIN nomina.concepto_cuentas_contables ccc
          ON ccc.codigo_concepto = cc.codigo_concepto
         AND ccc.id_agencia = cc.id_agencia
         AND COALESCE(ccc.activo, TRUE) = TRUE
        WHERE COALESCE(cc.valor, 0) <> 0

        UNION ALL

        SELECT
            cc.id_agencia,
            ccc.id_cuenta_credito AS id_catalogo_cuenta,
            cc.id_tercero,
            cc.id_empleado_referencia,
            0::numeric AS debito,
            cc.valor AS credito,
            cc.valor_base
        FROM conceptos_calculados cc
        JOIN nomina.concepto_cuentas_contables ccc
          ON ccc.codigo_concepto = cc.codigo_concepto
         AND ccc.id_agencia = cc.id_agencia
         AND COALESCE(ccc.activo, TRUE) = TRUE
        WHERE COALESCE(cc.valor, 0) <> 0
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
        nombre_empleado_referencia,
        nombre_tercero
    """.formatted(filtroAgencia);
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

    public List<String> listarCuentasNoOperablesEnPreview(Integer idPeriodoNomina, Integer idAgencia) {

        String sql = """
    WITH periodo_base AS (
        SELECT
            p.id_agencia,
            p.anio,
            p.mes
        FROM nomina.periodos_nomina p
        WHERE p.id_periodo = :idPeriodoNomina
    ),
    periodo_mes AS (
        SELECT
            p.id_agencia,
            MIN(p.fecha_inicio) AS fecha_inicio_mes,
            MAX(p.fecha_fin) AS fecha_fin_mes
        FROM nomina.periodos_nomina p
        JOIN periodo_base b
          ON b.id_agencia = p.id_agencia
         AND b.anio = p.anio
         AND b.mes = p.mes
        GROUP BY p.id_agencia
    ),
    contratos_base AS (
        SELECT
            e.id_agencia,
            COALESCE(ec.liquida_prima_semestral, FALSE) AS liquida_prima_semestral
        FROM periodo_mes pm
        JOIN nomina.empleados e
          ON e.id_agencia = pm.id_agencia
        JOIN nomina.empleado_contratos ec
          ON ec.id_empleado = e.id_empleado
        WHERE e.id_agencia = :idAgencia
          AND COALESCE(ec.activo, TRUE) = TRUE
          AND ec.fecha_inicio <= pm.fecha_fin_mes
          AND (ec.fecha_fin IS NULL OR ec.fecha_fin >= pm.fecha_inicio_mes)
    ),
    conceptos AS (
        SELECT id_agencia, 'PROV_CESANTIAS' AS codigo_concepto FROM contratos_base
        UNION
        SELECT id_agencia, 'PROV_INT_CES' FROM contratos_base
        UNION
        SELECT id_agencia, 'PRIMA_SERV' FROM contratos_base
        UNION
        SELECT id_agencia, 'PROV_VACACIONES' FROM contratos_base
        UNION
        SELECT id_agencia, 'PRIMA_SEM'
        FROM contratos_base
        WHERE liquida_prima_semestral = TRUE
    ),
    movimientos AS (
        SELECT c.id_agencia, ccc.id_cuenta_debito AS id_catalogo_cuenta
        FROM conceptos c
        JOIN nomina.concepto_cuentas_contables ccc
          ON ccc.codigo_concepto = c.codigo_concepto
         AND ccc.id_agencia = c.id_agencia
         AND COALESCE(ccc.activo, TRUE) = TRUE

        UNION

        SELECT c.id_agencia, ccc.id_cuenta_credito AS id_catalogo_cuenta
        FROM conceptos c
        JOIN nomina.concepto_cuentas_contables ccc
          ON ccc.codigo_concepto = c.codigo_concepto
         AND ccc.id_agencia = c.id_agencia
         AND COALESCE(ccc.activo, TRUE) = TRUE
    )
    SELECT DISTINCT
        cc.codigo_cuenta || ' - ' || cc.nombre_cuenta
    FROM movimientos m
    JOIN contabilidad.catalogo_cuentas cc
      ON cc.id_catalogo_cuenta = m.id_catalogo_cuenta
    WHERE COALESCE(cc.cuenta_operable, FALSE) = FALSE
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

    public String obtenerNumeroComprobantePrestaciones(Integer idPeriodoNomina, Integer idAgencia) {

        String sql = """
    WITH periodo_base AS (
        SELECT id_agencia, anio, mes
        FROM nomina.periodos_nomina
        WHERE id_periodo = :idPeriodoNomina
    )
    SELECT oc.numero_comprobante
    FROM contabilidad.origen_comprobantes oc
    JOIN periodo_base b
      ON oc.id_agencia = b.id_agencia
    JOIN nomina.periodos_nomina p
      ON p.id_periodo = oc.id_origen
    WHERE oc.id_agencia = :idAgencia
      AND oc.modulo_origen = 'NOMINA'
      AND oc.proceso_origen = 'PRESTACIONES_SOCIALES_NOMINA'
      AND oc.estado_origen = 'ACTIVO'
      AND p.anio = b.anio
      AND p.mes = b.mes
    LIMIT 1
    """;

        List<String> res = jdbc.queryForList(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodoNomina", idPeriodoNomina)
                        .addValue("idAgencia", idAgencia),
                String.class
        );

        return res.isEmpty() ? null : res.get(0);
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

    public String obtenerComprobanteExistente(Integer idPeriodoNomina) {

        String sql = """
        SELECT numero_comprobante
        FROM contabilidad.origen_comprobantes
        WHERE modulo_origen = 'NOMINA'
          AND proceso_origen = 'PRESTACIONES_SOCIALES_NOMINA'
          AND tabla_origen = 'nomina.empleado_contratos'
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
    WITH periodo_base AS (
        SELECT id_agencia, anio, mes
        FROM nomina.periodos_nomina
        WHERE id_periodo = :idPeriodoNomina
    )
    UPDATE contabilidad.origen_comprobantes oc
       SET estado_origen = 'REVERSADO',
           fk_seguridad_edicion = :idUsuario,
           fecha_edicion = CURRENT_TIMESTAMP
    FROM periodo_base b
    JOIN nomina.periodos_nomina p
      ON p.id_agencia = b.id_agencia
     AND p.anio = b.anio
     AND p.mes = b.mes
    WHERE oc.modulo_origen = 'NOMINA'
      AND oc.proceso_origen = 'PRESTACIONES_SOCIALES_NOMINA'
      AND oc.estado_origen = 'ACTIVO'
      AND oc.id_agencia = b.id_agencia
      AND oc.id_origen = p.id_periodo
    """;

        jdbc.update(sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodoNomina", idPeriodoNomina)
                        .addValue("idUsuario", idUsuario));
    }

    public record PeriodoMesBaseDTO(
            Integer idAgencia,
            Integer anio,
            Integer mes,
            LocalDate fechaInicioMes,
            LocalDate fechaFinMes
    ) {}

    public PeriodoMesBaseDTO obtenerPeriodoMesBase(Integer idPeriodoNomina) {

        String sql = """
        SELECT
            id_agencia,
            anio,
            mes,
            MIN(fecha_inicio) OVER (PARTITION BY id_agencia, anio, mes) AS fecha_inicio_mes,
            MAX(fecha_fin) OVER (PARTITION BY id_agencia, anio, mes) AS fecha_fin_mes
        FROM nomina.periodos_nomina
        WHERE id_periodo = :idPeriodoNomina
        """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                (rs, rowNum) -> new PeriodoMesBaseDTO(
                        rs.getInt("id_agencia"),
                        rs.getInt("anio"),
                        rs.getInt("mes"),
                        rs.getObject("fecha_inicio_mes", LocalDate.class),
                        rs.getObject("fecha_fin_mes", LocalDate.class)
                )
        );
    }

    public boolean periodosDelMesEstanContabilizados(Integer idPeriodoNomina) {

        String sql = """
        WITH periodo_base AS (
            SELECT id_agencia, anio, mes
            FROM nomina.periodos_nomina
            WHERE id_periodo = :idPeriodoNomina
        )
        SELECT
            COUNT(1) AS total_periodos,
            SUM(CASE WHEN p.estado = 'CONTABILIZADO' THEN 1 ELSE 0 END) AS total_contabilizados
        FROM nomina.periodos_nomina p
        JOIN periodo_base b
          ON b.id_agencia = p.id_agencia
         AND b.anio = p.anio
         AND b.mes = p.mes
        """;

        return jdbc.query(sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                rs -> {
                    if (!rs.next()) return false;

                    int total = rs.getInt("total_periodos");
                    int contabilizados = rs.getInt("total_contabilizados");

                    return total > 0 && total == contabilizados;
                });
    }

    public List<Integer> listarPeriodosDelMes(Integer idPeriodoNomina) {

        String sql = """
        WITH periodo_base AS (
            SELECT id_agencia, anio, mes
            FROM nomina.periodos_nomina
            WHERE id_periodo = :idPeriodoNomina
        )
        SELECT p.id_periodo
        FROM nomina.periodos_nomina p
        JOIN periodo_base b
          ON b.id_agencia = p.id_agencia
         AND b.anio = p.anio
         AND b.mes = p.mes
        ORDER BY p.fecha_inicio, p.id_periodo
        """;

        return jdbc.queryForList(
                sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                Integer.class
        );
    }

}
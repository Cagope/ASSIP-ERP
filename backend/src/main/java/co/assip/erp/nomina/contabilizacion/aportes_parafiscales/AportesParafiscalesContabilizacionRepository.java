package co.assip.erp.nomina.contabilizacion.aportes_parafiscales;

import co.assip.erp.nomina.contabilizacion.liquidacion.dto.LiquidacionMovimientoContableDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AportesParafiscalesContabilizacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

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

    public List<Integer> listarAgenciasConMovimientos(Integer idPeriodoNomina) {
        String sql = """
        WITH periodo_base AS (
            SELECT id_agencia, anio, mes
            FROM nomina.periodos_nomina
            WHERE id_periodo = :idPeriodoNomina
        )
        SELECT DISTINCT e.id_agencia
        FROM nomina.liquidaciones l
        JOIN nomina.periodos_nomina p
          ON p.id_periodo = l.id_periodo_nomina
        JOIN periodo_base b
          ON b.id_agencia = p.id_agencia
         AND b.anio = p.anio
         AND b.mes = p.mes
        JOIN nomina.empleado_contratos ec
          ON ec.id_contrato = l.id_contrato
        JOIN nomina.empleados e
          ON e.id_empleado = ec.id_empleado
        ORDER BY e.id_agencia
        """;

        return jdbc.queryForList(
                sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                Integer.class
        );
    }

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
            SELECT p.id_agencia, p.anio, p.mes
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
        variables AS (
            SELECT
                COALESCE(vv.aplica_caja_compensacion, TRUE) AS aplica_caja_compensacion,
                COALESCE(vv.aplica_sena, TRUE) AS aplica_sena,
                COALESCE(vv.aplica_icbf, TRUE) AS aplica_icbf
            FROM nomina.variables_vigencia vv
            CROSS JOIN periodo_mes pm
            WHERE vv.activo = TRUE
              AND pm.fecha_fin_mes BETWEEN vv.fecha_inicial AND vv.fecha_final
            ORDER BY vv.fecha_inicial DESC
            LIMIT 1
        ),
        contratos_base AS (
              SELECT DISTINCT
                  e.id_agencia,
                  COALESCE(tc.aplica_salud, FALSE) AS aplica_salud,
                  COALESCE(tc.aplica_pension, FALSE) AS aplica_pension,
                  COALESCE(tc.aplica_arl, FALSE) AS aplica_arl,
                  COALESCE(tc.aplica_caja_compensacion, FALSE) AS aplica_caja_compensacion,
                  COALESCE(tc.aplica_parafiscales, FALSE) AS aplica_parafiscales
              FROM nomina.liquidaciones l
              JOIN nomina.periodos_nomina p
                ON p.id_periodo = l.id_periodo_nomina
              JOIN periodo_base b
                ON b.id_agencia = p.id_agencia
               AND b.anio = p.anio
               AND b.mes = p.mes
              JOIN nomina.empleado_contratos ec
                ON ec.id_contrato = l.id_contrato
              JOIN nomina.empleados e
                ON e.id_empleado = ec.id_empleado
              JOIN nomina.tipos_contrato tc
                ON tc.id_tipo_contrato = ec.id_tipo_contrato
              WHERE e.id_agencia = :idAgencia
        ),
        
        conceptos AS (
            SELECT id_agencia, 'SALUD_EMPRESA' AS codigo_concepto
            FROM contratos_base
            WHERE aplica_salud = TRUE

            UNION

            SELECT id_agencia, 'PENSION_EMPRESA'
            FROM contratos_base
            WHERE aplica_pension = TRUE

            UNION

            SELECT id_agencia, 'ARL_EMPRESA'
            FROM contratos_base
            WHERE aplica_arl = TRUE

            UNION

            SELECT cb.id_agencia, 'CAJA_COMP'
            FROM contratos_base cb
            CROSS JOIN variables v
            WHERE cb.aplica_caja_compensacion = TRUE
              AND cb.aplica_parafiscales = TRUE
              AND v.aplica_caja_compensacion = TRUE

            UNION

            SELECT cb.id_agencia, 'SENA'
            FROM contratos_base cb
            CROSS JOIN variables v
            WHERE cb.aplica_parafiscales = TRUE
              AND v.aplica_sena = TRUE

            UNION

            SELECT cb.id_agencia, 'ICBF'
            FROM contratos_base cb
            CROSS JOIN variables v
            WHERE cb.aplica_parafiscales = TRUE
              AND v.aplica_icbf = TRUE
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

    public void bloquearPeriodoNomina(Integer idPeriodoNomina) {
        String sql = """
            SELECT id_periodo
            FROM nomina.periodos_nomina
            WHERE id_periodo = :idPeriodoNomina
            FOR UPDATE
            """;

        jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                Integer.class
        );
    }

    public String obtenerNumeroComprobante(Integer idPeriodoNomina, Integer idAgencia) {
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
              AND oc.proceso_origen = 'APORTES_PARAFISCALES_NOMINA'
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
            WHERE oc.modulo_origen = 'NOMINA'
              AND oc.proceso_origen = 'APORTES_PARAFISCALES_NOMINA'
              AND oc.estado_origen = 'ACTIVO'
              AND p.id_agencia = b.id_agencia
              AND p.anio = b.anio
              AND p.mes = b.mes
            LIMIT 1
            """;

        List<String> res = jdbc.queryForList(
                sql,
                new MapSqlParameterSource("idPeriodoNomina", idPeriodoNomina),
                String.class
        );

        return res.isEmpty() ? null : res.get(0);
    }

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
              AND oc.proceso_origen = 'APORTES_PARAFISCALES_NOMINA'
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

    private String buildPreviewSql(boolean filtrarAgencia) {
        String filtroAgencia = filtrarAgencia ? " AND b.id_agencia = :idAgencia " : "";

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
                GROUP BY p.id_agencia, p.anio, p.mes
            ),

            variables AS (
                SELECT
                    vv.smmlv,
                    vv.porc_salud_empleado,
                    vv.porc_salud_empleador,
                    vv.porc_pension_empleador,
                    vv.porc_caja_compensacion,
                    vv.porc_sena,
                    vv.porc_icbf,
                    vv.tope_ibc_min_smmlv,
                    COALESCE(vv.aplica_caja_compensacion, TRUE) AS aplica_caja_compensacion,
                    COALESCE(vv.aplica_sena, TRUE) AS aplica_sena,
                    COALESCE(vv.aplica_icbf, TRUE) AS aplica_icbf
                FROM nomina.variables_vigencia vv
                CROSS JOIN periodo_mes pm
                WHERE vv.activo = TRUE
                  AND pm.fecha_fin_mes BETWEEN vv.fecha_inicial AND vv.fecha_final
                ORDER BY vv.fecha_inicial DESC
                LIMIT 1
            ),

            contratos_base AS (
                  SELECT DISTINCT
                      e.id_agencia,
                      e.id_empleado,
                      e.id_datos_personal,
                      ec.id_contrato,
                      COALESCE(ec.salario_base, 0)::numeric AS salario_base,
                      COALESCE(ec.porcentaje_arl, 0)::numeric AS porcentaje_arl,
                      tc.codigo AS codigo_tipo_contrato,
            
                      ec.id_eps,
                      ec.id_afp,
                      ec.id_arl,
                      ec.id_caja_compensacion,
            
                      eps.id_datos_personal AS id_tercero_eps,
                      afp.id_datos_personal AS id_tercero_afp,
                      arl.id_datos_personal AS id_tercero_arl,
                      cc.id_datos_personal AS id_tercero_caja,
            
                      COALESCE(tc.aplica_salud, FALSE) AS aplica_salud,
                      COALESCE(tc.aplica_pension, FALSE) AS aplica_pension,
                      COALESCE(tc.aplica_arl, FALSE) AS aplica_arl,
                      COALESCE(tc.aplica_caja_compensacion, FALSE) AS aplica_caja_compensacion,
                      COALESCE(tc.aplica_parafiscales, FALSE) AS aplica_parafiscales
                  FROM nomina.liquidaciones l
                  JOIN nomina.periodos_nomina p
                    ON p.id_periodo = l.id_periodo_nomina
                  JOIN periodo_base b
                    ON b.id_agencia = p.id_agencia
                   AND b.anio = p.anio
                   AND b.mes = p.mes
                  JOIN nomina.empleado_contratos ec
                    ON ec.id_contrato = l.id_contrato
                  JOIN nomina.empleados e
                    ON e.id_empleado = ec.id_empleado
                  JOIN nomina.tipos_contrato tc
                    ON tc.id_tipo_contrato = ec.id_tipo_contrato
                  LEFT JOIN nomina.entidades_eps eps
                    ON eps.id_eps = ec.id_eps
                  LEFT JOIN nomina.entidades_afp afp
                    ON afp.id_afp = ec.id_afp
                  LEFT JOIN nomina.entidades_arl arl
                    ON arl.id_arl = ec.id_arl
                  LEFT JOIN nomina.entidades_caja_compensacion cc
                    ON cc.id_caja = ec.id_caja_compensacion
                  WHERE 1 = 1
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

            base_ibc AS (
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
                WHERE COALESCE(cn.afecta_ibc, FALSE) = TRUE
                   OR dm.codigo_concepto IS NULL
                GROUP BY cb.id_agencia, cb.id_contrato, cb.id_datos_personal
            ),

            base_arl AS (
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
                WHERE COALESCE(cn.afecta_base_arl, FALSE) = TRUE
                   OR dm.codigo_concepto IS NULL
                GROUP BY cb.id_agencia, cb.id_contrato, cb.id_datos_personal
            ),

            base_parafiscales AS (
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
                WHERE COALESCE(cn.afecta_base_parafiscales, FALSE) = TRUE
                   OR dm.codigo_concepto IS NULL
                GROUP BY cb.id_agencia, cb.id_contrato, cb.id_datos_personal
            ),

            conceptos_calculados AS (

                -- SALUD EMPLEADOR
                SELECT
                    cb.id_agencia,
                    cb.id_tercero_eps AS id_tercero,
                    cb.id_datos_personal AS id_empleado_referencia,
                    'SALUD_EMPRESA' AS codigo_concepto,
                    ROUND(
                        bi.valor_base *
                        (
                            CASE
                                WHEN cb.codigo_tipo_contrato = 'APRENDIZ SENA'
                                    THEN ((v.porc_salud_empleado + v.porc_salud_empleador) / 100.0)
                                WHEN bi.valor_base >= (v.smmlv * v.tope_ibc_min_smmlv)
                                    THEN (v.porc_salud_empleador / 100.0)
                                ELSE 0
                            END
                        )
                    , 0) AS valor,
                    bi.valor_base AS valor_base
                FROM contratos_base cb
                JOIN base_ibc bi
                  ON bi.id_agencia = cb.id_agencia
                 AND bi.id_contrato = cb.id_contrato
                 AND bi.id_datos_personal = cb.id_datos_personal
                CROSS JOIN variables v
                WHERE cb.aplica_salud = TRUE

                UNION ALL

                -- PENSION EMPLEADOR
                SELECT
                    cb.id_agencia,
                    cb.id_tercero_afp AS id_tercero,
                    cb.id_datos_personal AS id_empleado_referencia,
                    'PENSION_EMPRESA' AS codigo_concepto,
                    CEIL((bi.valor_base * (v.porc_pension_empleador / 100.0)) / 100.0) * 100 AS valor,
                    bi.valor_base AS valor_base
                FROM contratos_base cb
                JOIN base_ibc bi
                  ON bi.id_agencia = cb.id_agencia
                 AND bi.id_contrato = cb.id_contrato
                 AND bi.id_datos_personal = cb.id_datos_personal
                CROSS JOIN variables v
                WHERE cb.aplica_pension = TRUE
                  AND cb.codigo_tipo_contrato <> 'APRENDIZ SENA'

                UNION ALL

                -- ARL EMPLEADOR
                SELECT
                    cb.id_agencia,
                    cb.id_tercero_arl AS id_tercero,
                    cb.id_datos_personal AS id_empleado_referencia,
                    'ARL_EMPRESA' AS codigo_concepto,
                    CEIL((ba.valor_base * (cb.porcentaje_arl / 100.0)) / 100.0) * 100 AS valor,
                    ba.valor_base AS valor_base
                FROM contratos_base cb
                JOIN base_arl ba
                  ON ba.id_agencia = cb.id_agencia
                 AND ba.id_contrato = cb.id_contrato
                 AND ba.id_datos_personal = cb.id_datos_personal
                WHERE cb.aplica_arl = TRUE

                UNION ALL

                -- CAJA
                SELECT
                    cb.id_agencia,
                    cb.id_tercero_caja AS id_tercero,
                    cb.id_datos_personal AS id_empleado_referencia,
                    'CAJA_COMP' AS codigo_concepto,
                    CEIL((bp.valor_base * (v.porc_caja_compensacion / 100.0)) / 100.0) * 100 AS valor,
                    bp.valor_base AS valor_base
                FROM contratos_base cb
                JOIN base_parafiscales bp
                  ON bp.id_agencia = cb.id_agencia
                 AND bp.id_contrato = cb.id_contrato
                 AND bp.id_datos_personal = cb.id_datos_personal
                CROSS JOIN variables v
                WHERE cb.aplica_caja_compensacion = TRUE
                  AND cb.aplica_parafiscales = TRUE
                  AND v.aplica_caja_compensacion = TRUE

                UNION ALL

                -- SENA
                SELECT
                    cb.id_agencia,
                    NULL::integer AS id_tercero,
                    cb.id_datos_personal AS id_empleado_referencia,
                    'SENA' AS codigo_concepto,
                    CEIL((bp.valor_base * (v.porc_sena / 100.0)) / 100.0) * 100 AS valor,
                    bp.valor_base AS valor_base
                FROM contratos_base cb
                JOIN base_parafiscales bp
                  ON bp.id_agencia = cb.id_agencia
                 AND bp.id_contrato = cb.id_contrato
                 AND bp.id_datos_personal = cb.id_datos_personal
                CROSS JOIN variables v
                WHERE cb.aplica_parafiscales = TRUE
                  AND v.aplica_sena = TRUE

                UNION ALL

                -- ICBF
                SELECT
                    cb.id_agencia,
                    NULL::integer AS id_tercero,
                    cb.id_datos_personal AS id_empleado_referencia,
                    'ICBF' AS codigo_concepto,
                    CEIL((bp.valor_base * (v.porc_icbf / 100.0)) / 100.0) * 100 AS valor,
                    bp.valor_base AS valor_base
                FROM contratos_base cb
                JOIN base_parafiscales bp
                  ON bp.id_agencia = cb.id_agencia
                 AND bp.id_contrato = cb.id_contrato
                 AND bp.id_datos_personal = cb.id_datos_personal
                CROSS JOIN variables v
                WHERE cb.aplica_parafiscales = TRUE
                  AND v.aplica_icbf = TRUE
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
}
package co.assip.erp.gerencia.dashboard_cdat;

import co.assip.erp.gerencia.dashboard_cdat.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardCdatRepository {

    private final JdbcTemplate jdbc;

    public DashboardCdatResumenDTO obtenerResumen(LocalDate fechaCorte) {

        String sql = """
        SELECT
            COUNT(*) AS total_cdats,
            COALESCE(SUM(v.saldo_actual_cdat), 0) AS valor_total,
            COALESCE(AVG(v.tasa_nominal_anual), 0) AS promedio_tasa,
            COALESCE(AVG(v.plazo_meses), 0) AS promedio_plazo,

            COUNT(*) FILTER (
                WHERE v.fecha_vencimiento_cdat
                BETWEEN ? AND (? + INTERVAL '30 DAY')
            ) AS vencen_30,

            COUNT(*) FILTER (
                WHERE v.id_cuenta_cdat_origen IS NOT NULL
                AND v.fecha_apertura_cdat
                    BETWEEN date_trunc('month', ?::date)
                    AND ?
            ) AS renovaciones_mes,

            COUNT(*) FILTER (
                WHERE v.fecha_cancelacion_cdat
                    BETWEEN date_trunc('month', ?::date)
                    AND ?
            ) AS cancelaciones_mes

        FROM cdat.vw_cdat_cuentas_total_extendida v
        WHERE v.fecha_apertura_cdat <= ?
        """;

        return jdbc.queryForObject(
                sql,
                (rs, rowNum) ->
                        DashboardCdatResumenDTO.builder()
                                .totalCdats(rs.getInt("total_cdats"))
                                .valorTotalCaptado(rs.getBigDecimal("valor_total"))
                                .promedioTasa(rs.getBigDecimal("promedio_tasa"))
                                .promedioPlazo(rs.getBigDecimal("promedio_plazo"))
                                .vencen30Dias(rs.getInt("vencen_30"))
                                .renovacionesMes(rs.getInt("renovaciones_mes"))
                                .cancelacionesMes(rs.getInt("cancelaciones_mes"))
                                .build(),
                fechaCorte,
                fechaCorte,
                fechaCorte,
                fechaCorte,
                fechaCorte,
                fechaCorte,
                fechaCorte
        );
    }

    public List<DashboardCdatAgenciaDTO> obtenerAgencias(LocalDate fechaCorte) {

        String sql = """
            WITH agrupado AS (
                SELECT
                    COALESCE(v.nombre_agencia, 'Sin agencia') AS agencia,
                    COUNT(*) AS cantidad,
                    COALESCE(SUM(v.saldo_actual_cdat), 0) AS valor_total,
                    COALESCE(AVG(v.tasa_nominal_anual), 0) AS promedio_tasa
                FROM cdat.vw_cdat_cuentas_total_extendida v
                WHERE v.fecha_apertura_cdat <= ?
                GROUP BY COALESCE(v.nombre_agencia, 'Sin agencia')
            ),
            total AS (
                SELECT COALESCE(SUM(valor_total), 0) AS total_general
                FROM agrupado
            )
            SELECT
                a.agencia,
                a.cantidad,
                a.valor_total,
                a.promedio_tasa,
                CASE
                    WHEN t.total_general = 0 THEN 0
                    ELSE ROUND((a.valor_total * 100.0) / t.total_general, 2)
                END AS participacion
            FROM agrupado a
            CROSS JOIN total t
            ORDER BY a.valor_total DESC
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatAgenciaDTO.builder()
                                .agencia(rs.getString("agencia"))
                                .cantidad(rs.getInt("cantidad"))
                                .valorTotal(rs.getBigDecimal("valor_total"))
                                .promedioTasa(rs.getBigDecimal("promedio_tasa"))
                                .participacion(rs.getBigDecimal("participacion"))
                                .build(),
                fechaCorte
        );
    }

    public List<DashboardCdatGrupoDTO> obtenerPlazos(LocalDate fechaCorte) {

        String sql = """
            WITH base AS (
                SELECT
                    CASE
                        WHEN v.plazo_meses < 6 THEN 1
                        WHEN v.plazo_meses BETWEEN 6 AND 12 THEN 2
                        WHEN v.plazo_meses > 12 AND v.plazo_meses <= 18 THEN 3
                        ELSE 4
                    END AS orden,

                    CASE
                        WHEN v.plazo_meses < 6 THEN 'MENOR A 6 MESES'
                        WHEN v.plazo_meses BETWEEN 6 AND 12 THEN '6 A 12 MESES'
                        WHEN v.plazo_meses > 12 AND v.plazo_meses <= 18 THEN '12 A 18 MESES'
                        ELSE 'MAS DE 18 MESES'
                    END AS concepto,

                    v.saldo_actual_cdat
                FROM cdat.vw_cdat_cuentas_total_extendida v
                WHERE v.fecha_apertura_cdat <= ?
            ),
            agrupado AS (
                SELECT
                    orden,
                    concepto,
                    COUNT(*) AS cantidad,
                    COALESCE(SUM(saldo_actual_cdat), 0) AS valor_total
                FROM base
                GROUP BY orden, concepto
            ),
            total AS (
                SELECT COALESCE(SUM(valor_total), 0) AS total_general
                FROM agrupado
            )
            SELECT
                a.concepto,
                a.cantidad,
                a.valor_total,
                CASE
                    WHEN t.total_general = 0 THEN 0
                    ELSE ROUND((a.valor_total * 100.0) / t.total_general, 2)
                END AS participacion
            FROM agrupado a
            CROSS JOIN total t
            ORDER BY a.orden
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatGrupoDTO.builder()
                                .concepto(rs.getString("concepto"))
                                .cantidad(rs.getInt("cantidad"))
                                .valorTotal(rs.getBigDecimal("valor_total"))
                                .participacion(rs.getBigDecimal("participacion"))
                                .build(),
                fechaCorte
        );
    }

    public List<DashboardCdatGrupoDTO> obtenerTasas(LocalDate fechaCorte) {

        String sql = """
            WITH base AS (
                SELECT
                    CASE
                        WHEN v.tasa_nominal_anual <= 5 THEN '0% - 5%'
                        WHEN v.tasa_nominal_anual <= 8 THEN '5.01% - 8%'
                        WHEN v.tasa_nominal_anual <= 10 THEN '8.01% - 10%'
                        ELSE 'MAS DE 10%'
                    END AS concepto,

                    v.saldo_actual_cdat
                FROM cdat.vw_cdat_cuentas_total_extendida v
                WHERE v.fecha_apertura_cdat <= ?
            ),
            agrupado AS (
                SELECT
                    concepto,
                    COUNT(*) AS cantidad,
                    COALESCE(SUM(saldo_actual_cdat), 0) AS valor_total
                FROM base
                GROUP BY concepto
            ),
            total AS (
                SELECT COALESCE(SUM(valor_total), 0) AS total_general
                FROM agrupado
            )
            SELECT
                a.concepto,
                a.cantidad,
                a.valor_total,
                CASE
                    WHEN t.total_general = 0 THEN 0
                    ELSE ROUND((a.valor_total * 100.0) / t.total_general, 2)
                END AS participacion
            FROM agrupado a
            CROSS JOIN total t
            ORDER BY a.concepto
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatGrupoDTO.builder()
                                .concepto(rs.getString("concepto"))
                                .cantidad(rs.getInt("cantidad"))
                                .valorTotal(rs.getBigDecimal("valor_total"))
                                .participacion(rs.getBigDecimal("participacion"))
                                .build(),
                fechaCorte
        );
    }

    public List<DashboardCdatTendenciaDTO> obtenerTendencia(LocalDate fechaCorte) {

        String sql = """
        WITH meses AS (
            SELECT
                generate_series(
                    date_trunc('month', ?::date) - INTERVAL '11 months',
                    date_trunc('month', ?::date),
                    INTERVAL '1 month'
                )::date AS mes
        ),
        datos AS (
            SELECT
                m.mes,

                COUNT(v.id_cuenta_cdat) FILTER (
                    WHERE v.id_cuenta_cdat_origen IS NULL
                    AND date_trunc('month', v.fecha_apertura_cdat) = m.mes
                ) AS aperturas,

                COUNT(v.id_cuenta_cdat) FILTER (
                    WHERE date_trunc('month', v.fecha_cancelacion_cdat) = m.mes
                ) AS cancelaciones,

                COUNT(v.id_cuenta_cdat) FILTER (
                    WHERE v.id_cuenta_cdat_origen IS NOT NULL
                    AND date_trunc('month', v.fecha_apertura_cdat) = m.mes
                ) AS renovaciones,

                COALESCE(SUM(v.saldo_actual_cdat) FILTER (
                    WHERE v.id_cuenta_cdat_origen IS NULL
                    AND date_trunc('month', v.fecha_apertura_cdat) = m.mes
                ), 0)
                +
                COALESCE(SUM(v.saldo_actual_cdat) FILTER (
                    WHERE v.id_cuenta_cdat_origen IS NOT NULL
                    AND date_trunc('month', v.fecha_apertura_cdat) = m.mes
                ), 0)
                -
                COALESCE(SUM(v.saldo_actual_cdat) FILTER (
                    WHERE date_trunc('month', v.fecha_cancelacion_cdat) = m.mes
                ), 0) AS captacion_neta

            FROM meses m
            LEFT JOIN cdat.vw_cdat_cuentas_total_extendida v
                ON (
                    date_trunc('month', v.fecha_apertura_cdat) = m.mes
                    OR date_trunc('month', v.fecha_cancelacion_cdat) = m.mes
                )
            GROUP BY m.mes
        )
        SELECT
            TO_CHAR(mes, 'YYYY-MM') AS periodo,
            aperturas,
            cancelaciones,
            renovaciones,
            captacion_neta
        FROM datos
        ORDER BY mes
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatTendenciaDTO.builder()
                                .periodo(rs.getString("periodo"))
                                .aperturas(rs.getInt("aperturas"))
                                .cancelaciones(rs.getInt("cancelaciones"))
                                .renovaciones(rs.getInt("renovaciones"))
                                .captacionNeta(rs.getBigDecimal("captacion_neta"))
                                .build(),
                fechaCorte,
                fechaCorte
        );
    }

    public List<DashboardCdatVencimientoDTO> obtenerVencimientos(LocalDate fechaCorte) {

        String sql = """
        WITH base AS (

            SELECT
                CASE
                    WHEN v.fecha_vencimiento_cdat = ? THEN 'HOY'
                    WHEN v.fecha_vencimiento_cdat <= (? + INTERVAL '7 DAY')
                        THEN '7 DIAS'
                    WHEN v.fecha_vencimiento_cdat <= (? + INTERVAL '15 DAY')
                        THEN '15 DIAS'
                    WHEN v.fecha_vencimiento_cdat <= (? + INTERVAL '30 DAY')
                        THEN '30 DIAS'
                END AS rango,

                v.saldo_actual_cdat

            FROM cdat.vw_cdat_cuentas_total_extendida v

            WHERE v.fecha_vencimiento_cdat
                BETWEEN ? AND (? + INTERVAL '30 DAY')
        ),
        agrupado AS (

            SELECT
                rango,
                COUNT(*) AS cantidad,
                COALESCE(SUM(saldo_actual_cdat), 0) AS valor_total
            FROM base
            WHERE rango IS NOT NULL
            GROUP BY rango

        ),
        total AS (

            SELECT
                COALESCE(SUM(valor_total), 0) AS total_general
            FROM agrupado

        )
        SELECT
            a.rango,
            a.cantidad,
            a.valor_total,

            CASE
                WHEN t.total_general = 0 THEN 0
                ELSE ROUND(
                    (a.valor_total * 100.0) / t.total_general,
                    2
                )
            END AS participacion

        FROM agrupado a
        CROSS JOIN total t

        ORDER BY
            CASE a.rango
                WHEN 'HOY' THEN 1
                WHEN '7 DIAS' THEN 2
                WHEN '15 DIAS' THEN 3
                WHEN '30 DIAS' THEN 4
            END
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatVencimientoDTO.builder()
                                .rango(rs.getString("rango"))
                                .cantidad(rs.getInt("cantidad"))
                                .valorTotal(rs.getBigDecimal("valor_total"))
                                .participacion(rs.getBigDecimal("participacion"))
                                .build(),
                fechaCorte,
                fechaCorte,
                fechaCorte,
                fechaCorte,
                fechaCorte,
                fechaCorte
        );
    }

}
package co.assip.erp.gerencia.dashboard_cdat;

import co.assip.erp.gerencia.dashboard_cdat.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardCdatRepository {

    private final JdbcTemplate jdbc;

    // =========================================================
    // RESUMEN GENERAL
    // =========================================================

    public DashboardCdatResumenDTO obtenerResumen() {

        String sql = """
            SELECT
                COUNT(*) AS total_cdats,

                COUNT(
                    DISTINCT v.id_datos_personal
                ) AS total_asociados,

                COALESCE(
                    SUM(v.saldo_actual_cdat),
                    0
                ) AS valor_total,

                COALESCE(
                    ROUND(
                        SUM(
                            v.tasa_nominal_anual
                            * v.saldo_actual_cdat
                        )
                        / NULLIF(
                            SUM(v.saldo_actual_cdat),
                            0
                        ),
                        2
                    ),
                    0
                ) AS promedio_tasa,

                COALESCE(
                    ROUND(
                        AVG(v.plazo_meses),
                        2
                    ),
                    0
                ) AS promedio_plazo,

                COUNT(*) FILTER (
                    WHERE v.fecha_vencimiento_cdat
                        BETWEEN CURRENT_DATE
                        AND CURRENT_DATE + INTERVAL '30 DAY'
                ) AS vencen_30

            FROM cdat.vw_cdat_cuentas_total_extendida v

            WHERE v.estado_cdat = 'A'
            """;

        return jdbc.queryForObject(
                sql,
                (rs, rowNum) ->
                        DashboardCdatResumenDTO.builder()
                                .totalCdats(
                                        rs.getInt("total_cdats")
                                )
                                .totalAsociados(
                                        rs.getInt("total_asociados")
                                )
                                .valorTotalCaptado(
                                        rs.getBigDecimal("valor_total")
                                )
                                .promedioTasa(
                                        rs.getBigDecimal("promedio_tasa")
                                )
                                .promedioPlazo(
                                        rs.getBigDecimal("promedio_plazo")
                                )
                                .vencen30Dias(
                                        rs.getInt("vencen_30")
                                )
                                .build()
        );
    }

    // =========================================================
    // DISTRIBUCIÓN POR AGENCIA
    // =========================================================

    public List<DashboardCdatAgenciaDTO> obtenerAgencias() {

        String sql = """
            WITH agrupado AS (
                SELECT
                    COALESCE(
                        v.nombre_agencia,
                        'Sin agencia'
                    ) AS agencia,

                    COUNT(*) AS cantidad,

                    COALESCE(
                        SUM(v.saldo_actual_cdat),
                        0
                    ) AS valor_total,

                    COALESCE(
                        ROUND(
                            SUM(
                                v.tasa_nominal_anual
                                * v.saldo_actual_cdat
                            )
                            / NULLIF(
                                SUM(v.saldo_actual_cdat),
                                0
                            ),
                            2
                        ),
                        0
                    ) AS promedio_tasa

                FROM cdat.vw_cdat_cuentas_total_extendida v

                WHERE v.estado_cdat = 'A'

                GROUP BY
                    COALESCE(
                        v.nombre_agencia,
                        'Sin agencia'
                    )
            ),

            total AS (
                SELECT
                    COALESCE(
                        SUM(valor_total),
                        0
                    ) AS total_general
                FROM agrupado
            )

            SELECT
                a.agencia,
                a.cantidad,
                a.valor_total,
                a.promedio_tasa,

                CASE
                    WHEN t.total_general = 0
                        THEN 0
                    ELSE ROUND(
                        (a.valor_total * 100.0)
                        / t.total_general,
                        2
                    )
                END AS participacion

            FROM agrupado a
            CROSS JOIN total t

            ORDER BY
                a.valor_total DESC
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatAgenciaDTO.builder()
                                .agencia(
                                        rs.getString("agencia")
                                )
                                .cantidad(
                                        rs.getInt("cantidad")
                                )
                                .valorTotal(
                                        rs.getBigDecimal("valor_total")
                                )
                                .promedioTasa(
                                        rs.getBigDecimal("promedio_tasa")
                                )
                                .participacion(
                                        rs.getBigDecimal("participacion")
                                )
                                .build()
        );
    }

    // =========================================================
    // DISTRIBUCIÓN POR PLAZO
    // =========================================================

    public List<DashboardCdatGrupoDTO> obtenerPlazos() {

        String sql = """
            WITH base AS (
                SELECT
                    CASE
                        WHEN v.plazo_meses < 6
                            THEN 1

                        WHEN v.plazo_meses BETWEEN 6 AND 12
                            THEN 2

                        WHEN v.plazo_meses > 12
                             AND v.plazo_meses <= 18
                            THEN 3

                        ELSE 4
                    END AS orden,

                    CASE
                        WHEN v.plazo_meses < 6
                            THEN 'MENOR A 6 MESES'

                        WHEN v.plazo_meses BETWEEN 6 AND 12
                            THEN '6 A 12 MESES'

                        WHEN v.plazo_meses > 12
                             AND v.plazo_meses <= 18
                            THEN '12 A 18 MESES'

                        ELSE 'MAS DE 18 MESES'
                    END AS concepto,

                    v.saldo_actual_cdat

                FROM cdat.vw_cdat_cuentas_total_extendida v

                WHERE v.estado_cdat = 'A'
            ),

            agrupado AS (
                SELECT
                    orden,
                    concepto,
                    COUNT(*) AS cantidad,

                    COALESCE(
                        SUM(saldo_actual_cdat),
                        0
                    ) AS valor_total

                FROM base

                GROUP BY
                    orden,
                    concepto
            ),

            total AS (
                SELECT
                    COALESCE(
                        SUM(valor_total),
                        0
                    ) AS total_general
                FROM agrupado
            )

            SELECT
                a.concepto,
                a.cantidad,
                a.valor_total,

                CASE
                    WHEN t.total_general = 0
                        THEN 0
                    ELSE ROUND(
                        (a.valor_total * 100.0)
                        / t.total_general,
                        2
                    )
                END AS participacion

            FROM agrupado a
            CROSS JOIN total t

            ORDER BY
                a.orden
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatGrupoDTO.builder()
                                .concepto(
                                        rs.getString("concepto")
                                )
                                .cantidad(
                                        rs.getInt("cantidad")
                                )
                                .valorTotal(
                                        rs.getBigDecimal("valor_total")
                                )
                                .participacion(
                                        rs.getBigDecimal("participacion")
                                )
                                .build()
        );
    }

    // =========================================================
    // DISTRIBUCIÓN POR TASA
    // =========================================================

    public List<DashboardCdatGrupoDTO> obtenerTasas() {

        String sql = """
            WITH base AS (
                SELECT
                    CASE
                        WHEN v.tasa_nominal_anual <= 5
                            THEN 1

                        WHEN v.tasa_nominal_anual <= 8
                            THEN 2

                        WHEN v.tasa_nominal_anual <= 10
                            THEN 3

                        ELSE 4
                    END AS orden,

                    CASE
                        WHEN v.tasa_nominal_anual <= 5
                            THEN '0% - 5%'

                        WHEN v.tasa_nominal_anual <= 8
                            THEN '5.01% - 8%'

                        WHEN v.tasa_nominal_anual <= 10
                            THEN '8.01% - 10%'

                        ELSE 'MAS DE 10%'
                    END AS concepto,

                    v.saldo_actual_cdat

                FROM cdat.vw_cdat_cuentas_total_extendida v

                WHERE v.estado_cdat = 'A'
            ),

            agrupado AS (
                SELECT
                    orden,
                    concepto,
                    COUNT(*) AS cantidad,

                    COALESCE(
                        SUM(saldo_actual_cdat),
                        0
                    ) AS valor_total

                FROM base

                GROUP BY
                    orden,
                    concepto
            ),

            total AS (
                SELECT
                    COALESCE(
                        SUM(valor_total),
                        0
                    ) AS total_general
                FROM agrupado
            )

            SELECT
                a.concepto,
                a.cantidad,
                a.valor_total,

                CASE
                    WHEN t.total_general = 0
                        THEN 0
                    ELSE ROUND(
                        (a.valor_total * 100.0)
                        / t.total_general,
                        2
                    )
                END AS participacion

            FROM agrupado a
            CROSS JOIN total t

            ORDER BY
                a.orden
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatGrupoDTO.builder()
                                .concepto(
                                        rs.getString("concepto")
                                )
                                .cantidad(
                                        rs.getInt("cantidad")
                                )
                                .valorTotal(
                                        rs.getBigDecimal("valor_total")
                                )
                                .participacion(
                                        rs.getBigDecimal("participacion")
                                )
                                .build()
        );
    }

    // =========================================================
    // PRÓXIMOS VENCIMIENTOS - RESUMEN
    //
    // HOY        = hoy
    // EN 10 DIAS = días 1 a 10
    // EN 20 DIAS = días 11 a 20
    // EN 30 DIAS = días 21 a 30
    // =========================================================

    public List<DashboardCdatVencimientoDTO> obtenerVencimientos() {

        String sql = """
            WITH base AS (
                SELECT
                    CASE
                        WHEN v.fecha_vencimiento_cdat = CURRENT_DATE
                            THEN 'HOY'

                        WHEN v.fecha_vencimiento_cdat
                            <= CURRENT_DATE + INTERVAL '10 DAY'
                            THEN 'EN 10 DIAS'

                        WHEN v.fecha_vencimiento_cdat
                            <= CURRENT_DATE + INTERVAL '20 DAY'
                            THEN 'EN 20 DIAS'

                        WHEN v.fecha_vencimiento_cdat
                            <= CURRENT_DATE + INTERVAL '30 DAY'
                            THEN 'EN 30 DIAS'
                    END AS rango,

                    v.saldo_actual_cdat

                FROM cdat.vw_cdat_cuentas_total_extendida v

                WHERE v.estado_cdat = 'A'

                  AND v.fecha_vencimiento_cdat
                      BETWEEN CURRENT_DATE
                      AND CURRENT_DATE + INTERVAL '30 DAY'
            ),

            agrupado AS (
                SELECT
                    rango,
                    COUNT(*) AS cantidad,

                    COALESCE(
                        SUM(saldo_actual_cdat),
                        0
                    ) AS valor_total

                FROM base

                WHERE rango IS NOT NULL

                GROUP BY
                    rango
            ),

            total AS (
                SELECT
                    COALESCE(
                        SUM(valor_total),
                        0
                    ) AS total_general
                FROM agrupado
            )

            SELECT
                a.rango,
                a.cantidad,
                a.valor_total,

                CASE
                    WHEN t.total_general = 0
                        THEN 0
                    ELSE ROUND(
                        (a.valor_total * 100.0)
                        / t.total_general,
                        2
                    )
                END AS participacion

            FROM agrupado a
            CROSS JOIN total t

            ORDER BY
                CASE a.rango
                    WHEN 'HOY' THEN 1
                    WHEN 'EN 10 DIAS' THEN 2
                    WHEN 'EN 20 DIAS' THEN 3
                    WHEN 'EN 30 DIAS' THEN 4
                END
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatVencimientoDTO.builder()
                                .rango(
                                        rs.getString("rango")
                                )
                                .cantidad(
                                        rs.getInt("cantidad")
                                )
                                .valorTotal(
                                        rs.getBigDecimal("valor_total")
                                )
                                .participacion(
                                        rs.getBigDecimal("participacion")
                                )
                                .build()
        );
    }

    // =========================================================
    // PRÓXIMOS VENCIMIENTOS - DETALLE
    // =========================================================

    public List<DashboardCdatVencimientoDetalleDTO>
    obtenerVencimientosDetalle(String rango) {

        String sql = """
            SELECT
                v.id_cuenta_cdat,
                v.codigo_cdat,

                COALESCE(
                    v.nombre_agencia,
                    ''
                ) AS agencia,

                v.id_datos_personal,

                COALESCE(
                    v.tipo_documento,
                    ''
                ) AS tipo_documento,

                COALESCE(
                    v.documento,
                    ''
                ) AS documento,

                COALESCE(
                    v.nombre_completo_apellidos,
                    v.nombre_completo_nombres,
                    ''
                ) AS nombre_completo,

                COALESCE(
                    v.telefono,
                    ''
                ) AS telefono,

                COALESCE(
                    v.celular_uno,
                    ''
                ) AS celular_uno,

                COALESCE(
                    v.celular_dos,
                    ''
                ) AS celular_dos,

                COALESCE(
                    v.correo_personal,
                    ''
                ) AS correo_personal,

                v.fecha_apertura_cdat,
                v.fecha_vencimiento_cdat,

                (
                    v.fecha_vencimiento_cdat
                    - CURRENT_DATE
                ) AS dias_para_vencer,

                v.plazo_meses,
                v.tasa_nominal_anual,
                v.saldo_actual_cdat

            FROM cdat.vw_cdat_cuentas_total_extendida v

            WHERE v.estado_cdat = 'A'

              AND v.fecha_vencimiento_cdat
                  BETWEEN CURRENT_DATE
                  AND CURRENT_DATE + INTERVAL '30 DAY'

              AND (
                    CASE
                        WHEN v.fecha_vencimiento_cdat = CURRENT_DATE
                            THEN 'HOY'

                        WHEN v.fecha_vencimiento_cdat
                            <= CURRENT_DATE + INTERVAL '10 DAY'
                            THEN 'EN 10 DIAS'

                        WHEN v.fecha_vencimiento_cdat
                            <= CURRENT_DATE + INTERVAL '20 DAY'
                            THEN 'EN 20 DIAS'

                        WHEN v.fecha_vencimiento_cdat
                            <= CURRENT_DATE + INTERVAL '30 DAY'
                            THEN 'EN 30 DIAS'
                    END
                  ) = ?

            ORDER BY
                v.fecha_vencimiento_cdat,
                v.saldo_actual_cdat DESC,
                v.codigo_cdat
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatVencimientoDetalleDTO.builder()
                                .idCuentaCdat(
                                        rs.getObject("id_cuenta_cdat") != null
                                                ? ((Number) rs.getObject("id_cuenta_cdat")).longValue()
                                                : null
                                )
                                .codigoCdat(
                                        rs.getString("codigo_cdat")
                                )
                                .agencia(
                                        rs.getString("agencia")
                                )
                                .idDatosPersonal(
                                        rs.getObject("id_datos_personal") != null
                                                ? ((Number) rs.getObject("id_datos_personal")).longValue()
                                                : null
                                )
                                .tipoDocumento(
                                        rs.getString("tipo_documento")
                                )
                                .documento(
                                        rs.getString("documento")
                                )
                                .nombreCompleto(
                                        rs.getString("nombre_completo")
                                )
                                .telefono(
                                        rs.getString("telefono")
                                )
                                .celularUno(
                                        rs.getString("celular_uno")
                                )
                                .celularDos(
                                        rs.getString("celular_dos")
                                )
                                .correoPersonal(
                                        rs.getString("correo_personal")
                                )
                                .fechaApertura(
                                        rs.getDate(
                                                "fecha_apertura_cdat"
                                        ) == null
                                                ? null
                                                : rs.getDate(
                                                "fecha_apertura_cdat"
                                        ).toLocalDate()
                                )
                                .fechaVencimiento(
                                        rs.getDate(
                                                "fecha_vencimiento_cdat"
                                        ) == null
                                                ? null
                                                : rs.getDate(
                                                "fecha_vencimiento_cdat"
                                        ).toLocalDate()
                                )
                                .diasParaVencer(
                                        rs.getObject(
                                                "dias_para_vencer",
                                                Integer.class
                                        )
                                )
                                .plazoMeses(
                                        rs.getObject(
                                                "plazo_meses",
                                                Integer.class
                                        )
                                )
                                .tasaNominalAnual(
                                        rs.getBigDecimal(
                                                "tasa_nominal_anual"
                                        )
                                )
                                .saldoActual(
                                        rs.getBigDecimal(
                                                "saldo_actual_cdat"
                                        )
                                )
                                .build(),
                rango
        );
    }

    // =========================================================
// COMPORTAMIENTO ÚLTIMOS 12 MESES
// =========================================================

    public List<DashboardCdatTendenciaDTO> obtenerTendencia12Meses() {

        String sql = """
        WITH cortes AS (
            SELECT DISTINCT
                c.fecha_corte
            FROM cdat.cierres_mensuales_cdats c
            WHERE c.estado = 'GENERADO'
            ORDER BY c.fecha_corte DESC
            LIMIT 12
        )

        SELECT
            c.fecha_corte,

            TO_CHAR(
                c.fecha_corte,
                'YYYY-MM'
            ) AS periodo,

            COUNT(*) AS cantidad_cdats,

            COALESCE(
                SUM(d.saldo_actual_cdat),
                0
            ) AS valor_captado

        FROM cortes x

        JOIN cdat.cierres_mensuales_cdats c
            ON c.fecha_corte = x.fecha_corte
           AND c.estado = 'GENERADO'

        JOIN cdat.cierres_mensuales_cdats_detalle d
            ON d.id_cierre_mensual_cdat =
               c.id_cierre_mensual_cdat

        WHERE d.estado_cdat = 'A'

        GROUP BY
            c.fecha_corte

        ORDER BY
            c.fecha_corte
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        DashboardCdatTendenciaDTO.builder()
                                .fechaCorte(
                                        rs.getDate(
                                                "fecha_corte"
                                        ).toLocalDate()
                                )
                                .periodo(
                                        rs.getString(
                                                "periodo"
                                        )
                                )
                                .cantidadCdats(
                                        rs.getInt(
                                                "cantidad_cdats"
                                        )
                                )
                                .valorCaptado(
                                        rs.getBigDecimal(
                                                "valor_captado"
                                        )
                                )
                                .build()
        );
    }
}
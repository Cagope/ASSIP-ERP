package co.assip.erp.cdat.analisis.concentracion;

import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatDepositanteDTO;
import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatDetalleDTO;
import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatResumenDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ConcentracionCdatRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ConcentracionCdatRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // BASE DEL ANÁLISIS
    // =========================================================

    private static final String BASE_CTE = """
            WITH base AS MATERIALIZED (
                SELECT
                    d.id_cuenta_cdat,
                    d.codigo_cdat,

                    d.id_datos_personal,
                    d.documento,
                    d.nombre_completo,

                    d.id_agencia,

                    d.fecha_apertura_cdat,
                    d.fecha_vencimiento_cdat,

                    d.plazo_meses,
                    d.plazo_dias,

                    d.tasa_nominal_anual,

                    d.valor_apertura_cdat,
                    d.saldo_actual_cdat

                FROM cdat.cierres_mensuales_cdats c

                JOIN cdat.cierres_mensuales_cdats_detalle d
                  ON d.id_cierre_mensual_cdat =
                     c.id_cierre_mensual_cdat

                WHERE c.fecha_corte = :fechaCorte
                  AND c.estado = 'GENERADO'
                  AND d.estado_cdat = 'A'
                  AND COALESCE(d.saldo_actual_cdat, 0) > 0
                  AND (
                        :idAgencia IS NULL
                        OR d.id_agencia = :idAgencia
                  )
            )
            """;

    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    public List<LocalDate> cortes() {

        String sql = """
                SELECT DISTINCT
                    c.fecha_corte
                FROM cdat.cierres_mensuales_cdats c
                WHERE c.estado = 'GENERADO'
                  AND c.fecha_corte IS NOT NULL
                ORDER BY c.fecha_corte DESC
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (rs, rowNum) ->
                        rs.getObject(
                                "fecha_corte",
                                LocalDate.class
                        )
        );
    }

    public LocalDate ultimoCorte() {

        String sql = """
                SELECT MAX(c.fecha_corte)
                FROM cdat.cierres_mensuales_cdats c
                WHERE c.estado = 'GENERADO'
                """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource(),
                LocalDate.class
        );
    }

    public boolean existeCorte(LocalDate fechaCorte) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM cdat.cierres_mensuales_cdats c
                    WHERE c.fecha_corte = :fechaCorte
                      AND c.estado = 'GENERADO'
                )
                """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte,
                                Types.DATE
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }

    // =========================================================
    // RESUMEN DE CONCENTRACIÓN
    // =========================================================

    public ConcentracionCdatResumenDTO resumen(
            LocalDate fechaCorte,
            Integer idAgencia
    ) {

        String sql = BASE_CTE + """
                ,
                depositantes AS MATERIALIZED (
                    SELECT
                        b.id_datos_personal,

                        COUNT(*) AS cantidad_cdats,

                        SUM(
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS saldo_depositante,

                        SUM(
                            COALESCE(
                                b.tasa_nominal_anual,
                                0
                            )
                            *
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS tasa_x_saldo,

                        SUM(
                            COALESCE(
                                b.plazo_meses,
                                0
                            )
                            *
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS plazo_x_saldo

                    FROM base b

                    GROUP BY
                        b.id_datos_personal
                ),
                totales AS MATERIALIZED (
                    SELECT
                        COUNT(*) AS total_cdats,

                        COUNT(
                            DISTINCT b.id_datos_personal
                        ) AS total_depositantes,

                        SUM(
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS saldo_total,

                        SUM(
                            COALESCE(
                                b.tasa_nominal_anual,
                                0
                            )
                            *
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS tasa_x_saldo,

                        SUM(
                            COALESCE(
                                b.plazo_meses,
                                0
                            )
                            *
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS plazo_x_saldo

                    FROM base b
                ),
                ranking AS MATERIALIZED (
                    SELECT
                        d.*,

                        ROW_NUMBER() OVER (
                            ORDER BY
                                d.saldo_depositante DESC,
                                d.id_datos_personal
                        ) AS posicion,

                        SUM(
                            d.saldo_depositante
                        ) OVER () AS saldo_general,

                        SUM(
                            d.saldo_depositante
                        ) OVER (
                            ORDER BY
                                d.saldo_depositante DESC,
                                d.id_datos_personal
                            ROWS BETWEEN
                                UNBOUNDED PRECEDING
                                AND CURRENT ROW
                        ) AS saldo_acumulado

                    FROM depositantes d
                ),
                metricas AS (
                    SELECT
                        SUM(
                            saldo_depositante
                        ) FILTER (
                            WHERE posicion <= 10
                        ) AS saldo_top_10,

                        SUM(
                            saldo_depositante
                        ) FILTER (
                            WHERE posicion <= 20
                        ) AS saldo_top_20,

                        SUM(
                            saldo_depositante
                        ) FILTER (
                            WHERE posicion <= 50
                        ) AS saldo_top_50,

                        SUM(
                            POWER(
                                saldo_depositante
                                /
                                NULLIF(
                                    saldo_general,
                                    0
                                ),
                                2
                            )
                        ) * 10000.0 AS hhi,

                        MIN(
                            posicion
                        ) FILTER (
                            WHERE
                                saldo_acumulado
                                /
                                NULLIF(
                                    saldo_general,
                                    0
                                ) >= 0.50
                        ) AS depositantes_50,

                        MIN(
                            posicion
                        ) FILTER (
                            WHERE
                                saldo_acumulado
                                /
                                NULLIF(
                                    saldo_general,
                                    0
                                ) >= 0.80
                        ) AS depositantes_80

                    FROM ranking
                )

                SELECT
                    :fechaCorte AS fecha_corte,

                    COALESCE(
                        t.total_cdats,
                        0
                    ) AS total_cdats,

                    COALESCE(
                        t.total_depositantes,
                        0
                    ) AS total_depositantes,

                    COALESCE(
                        t.saldo_total,
                        0
                    ) AS saldo_total,

                    CASE
                        WHEN COALESCE(
                            t.total_depositantes,
                            0
                        ) = 0
                        THEN 0
                        ELSE
                            t.saldo_total
                            /
                            t.total_depositantes
                    END AS saldo_promedio,

                    CASE
                        WHEN COALESCE(
                            t.saldo_total,
                            0
                        ) = 0
                        THEN 0
                        ELSE
                            t.tasa_x_saldo
                            /
                            t.saldo_total
                    END AS tasa_ponderada,

                    CASE
                        WHEN COALESCE(
                            t.saldo_total,
                            0
                        ) = 0
                        THEN 0
                        ELSE
                            t.plazo_x_saldo
                            /
                            t.saldo_total
                    END AS plazo_ponderado_meses,

                    CASE
                        WHEN COALESCE(
                            t.saldo_total,
                            0
                        ) = 0
                        THEN 0
                        ELSE
                            COALESCE(
                                m.saldo_top_10,
                                0
                            )
                            * 100.0
                            /
                            t.saldo_total
                    END AS participacion_top_10,

                    CASE
                        WHEN COALESCE(
                            t.saldo_total,
                            0
                        ) = 0
                        THEN 0
                        ELSE
                            COALESCE(
                                m.saldo_top_20,
                                0
                            )
                            * 100.0
                            /
                            t.saldo_total
                    END AS participacion_top_20,

                    CASE
                        WHEN COALESCE(
                            t.saldo_total,
                            0
                        ) = 0
                        THEN 0
                        ELSE
                            COALESCE(
                                m.saldo_top_50,
                                0
                            )
                            * 100.0
                            /
                            t.saldo_total
                    END AS participacion_top_50,

                    COALESCE(
                        m.hhi,
                        0
                    ) AS hhi,

                    COALESCE(
                        m.depositantes_50,
                        0
                    ) AS depositantes_50,

                    COALESCE(
                        m.depositantes_80,
                        0
                    ) AS depositantes_80

                FROM totales t
                CROSS JOIN metricas m
                """;

        return jdbc.queryForObject(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia
                ),
                (rs, rowNum) ->
                        ConcentracionCdatResumenDTO.builder()

                                .fechaCorte(
                                        rs.getObject(
                                                "fecha_corte",
                                                LocalDate.class
                                        )
                                )

                                .totalCdats(
                                        rs.getInt(
                                                "total_cdats"
                                        )
                                )

                                .totalDepositantes(
                                        rs.getInt(
                                                "total_depositantes"
                                        )
                                )

                                .saldoTotal(
                                        rs.getBigDecimal(
                                                "saldo_total"
                                        )
                                )

                                .saldoPromedio(
                                        rs.getBigDecimal(
                                                "saldo_promedio"
                                        )
                                )

                                .tasaPonderada(
                                        rs.getBigDecimal(
                                                "tasa_ponderada"
                                        )
                                )

                                .plazoPonderadoMeses(
                                        rs.getBigDecimal(
                                                "plazo_ponderado_meses"
                                        )
                                )

                                .participacionTop10(
                                        rs.getBigDecimal(
                                                "participacion_top_10"
                                        )
                                )

                                .participacionTop20(
                                        rs.getBigDecimal(
                                                "participacion_top_20"
                                        )
                                )

                                .participacionTop50(
                                        rs.getBigDecimal(
                                                "participacion_top_50"
                                        )
                                )

                                .hhi(
                                        rs.getBigDecimal(
                                                "hhi"
                                        )
                                )

                                .depositantesConcentran50(
                                        rs.getInt(
                                                "depositantes_50"
                                        )
                                )

                                .depositantesConcentran80(
                                        rs.getInt(
                                                "depositantes_80"
                                        )
                                )

                                .build()
        );
    }

    // =========================================================
    // RANKING DE DEPOSITANTES
    // =========================================================

    public List<ConcentracionCdatDepositanteDTO> ranking(
            LocalDate fechaCorte,
            Integer idAgencia
    ) {

        String sql = BASE_CTE + """
                ,
                depositantes AS MATERIALIZED (
                    SELECT
                        b.id_datos_personal,

                        MAX(
                            b.documento
                        ) AS documento,

                        MAX(
                            b.nombre_completo
                        ) AS nombre_completo,

                        COUNT(*) AS cantidad_cdats,

                        SUM(
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS saldo_total,

                        SUM(
                            COALESCE(
                                b.tasa_nominal_anual,
                                0
                            )
                            *
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS tasa_x_saldo,

                        SUM(
                            COALESCE(
                                b.plazo_meses,
                                0
                            )
                            *
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS plazo_x_saldo

                    FROM base b

                    GROUP BY
                        b.id_datos_personal
                ),
                ranking AS MATERIALIZED (
                    SELECT
                        d.*,

                        ROW_NUMBER() OVER (
                            ORDER BY
                                d.saldo_total DESC,
                                d.id_datos_personal
                        ) AS posicion,

                        SUM(
                            d.saldo_total
                        ) OVER () AS saldo_general,

                        SUM(
                            d.saldo_total
                        ) OVER (
                            ORDER BY
                                d.saldo_total DESC,
                                d.id_datos_personal
                            ROWS BETWEEN
                                UNBOUNDED PRECEDING
                                AND CURRENT ROW
                        ) AS saldo_acumulado

                    FROM depositantes d
                )

                SELECT
                    posicion,
                    id_datos_personal,
                    documento,
                    nombre_completo,
                    cantidad_cdats,
                    saldo_total,

                    CASE
                        WHEN saldo_general = 0
                        THEN 0
                        ELSE
                            saldo_total
                            * 100.0
                            /
                            saldo_general
                    END AS participacion,

                    CASE
                        WHEN saldo_general = 0
                        THEN 0
                        ELSE
                            saldo_acumulado
                            * 100.0
                            /
                            saldo_general
                    END AS participacion_acumulada,

                    CASE
                        WHEN saldo_total = 0
                        THEN 0
                        ELSE
                            tasa_x_saldo
                            /
                            saldo_total
                    END AS tasa_ponderada,

                    CASE
                        WHEN saldo_total = 0
                        THEN 0
                        ELSE
                            plazo_x_saldo
                            /
                            saldo_total
                    END AS plazo_ponderado_meses

                FROM ranking

                ORDER BY posicion
                """;

        return jdbc.query(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia
                ),
                (rs, rowNum) ->
                        ConcentracionCdatDepositanteDTO.builder()

                                .posicion(
                                        ((Number) rs.getObject(
                                                "posicion"
                                        )).intValue()
                                )

                                .idDatosPersonal(
                                        rs.getObject(
                                                "id_datos_personal"
                                        ) != null
                                                ? ((Number) rs.getObject(
                                                "id_datos_personal"
                                        )).longValue()
                                                : null
                                )

                                .documento(
                                        rs.getString(
                                                "documento"
                                        )
                                )

                                .nombreCompleto(
                                        rs.getString(
                                                "nombre_completo"
                                        )
                                )

                                .cantidadCdats(
                                        rs.getInt(
                                                "cantidad_cdats"
                                        )
                                )

                                .saldoTotal(
                                        rs.getBigDecimal(
                                                "saldo_total"
                                        )
                                )

                                .participacion(
                                        rs.getBigDecimal(
                                                "participacion"
                                        )
                                )

                                .participacionAcumulada(
                                        rs.getBigDecimal(
                                                "participacion_acumulada"
                                        )
                                )

                                .tasaPonderada(
                                        rs.getBigDecimal(
                                                "tasa_ponderada"
                                        )
                                )

                                .plazoPonderadoMeses(
                                        rs.getBigDecimal(
                                                "plazo_ponderado_meses"
                                        )
                                )

                                .build()
        );
    }

    // =========================================================
    // DETALLE DE CDAT POR DEPOSITANTE
    // =========================================================

    public List<ConcentracionCdatDetalleDTO> detalle(
            LocalDate fechaCorte,
            Long idDatosPersonal,
            Integer idAgencia
    ) {

        String sql = BASE_CTE + """
                ,
                total_depositante AS MATERIALIZED (
                    SELECT
                        SUM(
                            COALESCE(
                                b.saldo_actual_cdat,
                                0
                            )
                        ) AS saldo_depositante
                    FROM base b
                    WHERE b.id_datos_personal =
                          :idDatosPersonal
                )

                SELECT
                    b.id_cuenta_cdat,
                    b.codigo_cdat,

                    b.id_agencia,
                    da.codigo_agencia,
                    da.nombre_agencia,

                    b.fecha_apertura_cdat,
                    b.fecha_vencimiento_cdat,

                    b.plazo_meses,
                    b.plazo_dias,

                    b.tasa_nominal_anual,

                    b.valor_apertura_cdat,
                    b.saldo_actual_cdat,

                    CASE
                        WHEN COALESCE(
                            t.saldo_depositante,
                            0
                        ) = 0
                        THEN 0
                        ELSE
                            b.saldo_actual_cdat
                            * 100.0
                            /
                            t.saldo_depositante
                    END AS participacion_depositante

                FROM base b

                CROSS JOIN total_depositante t

                LEFT JOIN general.datos_agencias da
                  ON da.id_agencia = b.id_agencia

                WHERE b.id_datos_personal =
                      :idDatosPersonal

                ORDER BY
                    b.saldo_actual_cdat DESC,
                    b.codigo_cdat
                """;

        MapSqlParameterSource p =
                parametros(
                        fechaCorte,
                        idAgencia
                )
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal,
                                Types.BIGINT
                        );

        return jdbc.query(
                sql,
                p,
                (rs, rowNum) ->
                        ConcentracionCdatDetalleDTO.builder()

                                .idCuentaCdat(
                                        rs.getObject(
                                                "id_cuenta_cdat"
                                        ) != null
                                                ? ((Number) rs.getObject(
                                                "id_cuenta_cdat"
                                        )).longValue()
                                                : null
                                )

                                .codigoCdat(
                                        rs.getString(
                                                "codigo_cdat"
                                        )
                                )

                                .idAgencia(
                                        rs.getObject(
                                                "id_agencia"
                                        ) != null
                                                ? ((Number) rs.getObject(
                                                "id_agencia"
                                        )).intValue()
                                                : null
                                )

                                .codigoAgencia(
                                        rs.getString(
                                                "codigo_agencia"
                                        )
                                )

                                .nombreAgencia(
                                        rs.getString(
                                                "nombre_agencia"
                                        )
                                )

                                .fechaApertura(
                                        rs.getObject(
                                                "fecha_apertura_cdat",
                                                LocalDate.class
                                        )
                                )

                                .fechaVencimiento(
                                        rs.getObject(
                                                "fecha_vencimiento_cdat",
                                                LocalDate.class
                                        )
                                )

                                .plazoMeses(
                                        rs.getObject(
                                                "plazo_meses"
                                        ) != null
                                                ? ((Number) rs.getObject(
                                                "plazo_meses"
                                        )).intValue()
                                                : null
                                )

                                .plazoDias(
                                        rs.getObject(
                                                "plazo_dias"
                                        ) != null
                                                ? ((Number) rs.getObject(
                                                "plazo_dias"
                                        )).intValue()
                                                : null
                                )

                                .tasaNominalAnual(
                                        rs.getBigDecimal(
                                                "tasa_nominal_anual"
                                        )
                                )

                                .valorApertura(
                                        rs.getBigDecimal(
                                                "valor_apertura_cdat"
                                        )
                                )

                                .saldoActual(
                                        rs.getBigDecimal(
                                                "saldo_actual_cdat"
                                        )
                                )

                                .participacionDepositante(
                                        rs.getBigDecimal(
                                                "participacion_depositante"
                                        )
                                )

                                .build()
        );
    }

    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource parametros(
            LocalDate fechaCorte,
            Integer idAgencia
    ) {

        return new MapSqlParameterSource()

                .addValue(
                        "fechaCorte",
                        fechaCorte,
                        Types.DATE
                )

                .addValue(
                        "idAgencia",
                        idAgencia,
                        Types.INTEGER
                );
    }
}
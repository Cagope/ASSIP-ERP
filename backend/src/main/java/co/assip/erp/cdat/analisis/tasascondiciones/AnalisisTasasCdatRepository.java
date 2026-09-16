package co.assip.erp.cdat.analisis.tasascondiciones;

import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatCondicionDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatCorteDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatDetalleDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatRangoSaldoDTO;
import co.assip.erp.cdat.analisis.tasascondiciones.dto.AnalisisTasasCdatResumenDTO;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

@Repository
public class AnalisisTasasCdatRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AnalisisTasasCdatRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // BASE COMÚN
    // FOTOGRAFÍA DEL CIERRE MENSUAL CDAT
    // =========================================================

    private static final String BASE_SQL = """
        WITH base AS (
            SELECT
                d.id_cuenta_cdat,

                d.id_agencia,
                a.codigo_agencia,
                a.nombre_agencia,

                d.codigo_cdat,

                d.id_datos_personal,

                dp.tipo_documento,

                d.documento,
                d.nombre_completo,

                d.fecha_apertura_cdat,
                d.fecha_vencimiento_cdat,

                d.plazo_meses,
                d.plazo_dias,

                d.amortizacion_deposito,

                COALESCE(
                    am.nombre_amortizacion,
                    d.amortizacion_deposito
                ) AS nombre_amortizacion,

                d.valor_apertura_cdat,
                d.saldo_actual_cdat,

                d.tasa_nominal_anual,
                d.tasa_efectiva_anual,

                CASE
                    WHEN d.saldo_actual_cdat < 10000000
                        THEN '01. MENOR A 10 MILLONES'

                    WHEN d.saldo_actual_cdat < 50000000
                        THEN '02. 10 A 50 MILLONES'

                    WHEN d.saldo_actual_cdat < 100000000
                        THEN '03. 50 A 100 MILLONES'

                    WHEN d.saldo_actual_cdat < 250000000
                        THEN '04. 100 A 250 MILLONES'

                    WHEN d.saldo_actual_cdat < 500000000
                        THEN '05. 250 A 500 MILLONES'

                    ELSE
                        '06. 500 MILLONES O MAS'
                END AS rango_saldo

            FROM cdat.cierres_mensuales_cdats c

            INNER JOIN cdat.cierres_mensuales_cdats_detalle d
                    ON d.id_cierre_mensual_cdat =
                       c.id_cierre_mensual_cdat

            LEFT JOIN general.datos_agencias a
                   ON a.id_agencia = d.id_agencia

            LEFT JOIN hoja_vida.datos_personales dp
                   ON dp.id_datos_personal =
                      d.id_datos_personal

            LEFT JOIN cdat.amortizaciones_cdats am
                   ON am.codigo_amortizacion =
                      d.amortizacion_deposito

            WHERE
                c.fecha_corte = :fechaCorte
                AND c.estado = 'GENERADO'
                AND d.estado_cdat = 'A'
                AND d.saldo_actual_cdat > 0
                AND (:idAgencia IS NULL OR :idAgencia = 0 OR d.id_agencia = :idAgencia)

                AND (
                       :plazoMeses IS NULL
                    OR :plazoMeses = 0
                    OR d.plazo_meses = :plazoMeses
                )

                AND (
                       :amortizacion IS NULL
                    OR :amortizacion = ''
                    OR d.amortizacion_deposito =
                       :amortizacion
                )
        ),

        poblacion AS (
            SELECT *
            FROM base
            WHERE
                (
                       :rangoSaldo IS NULL
                    OR :rangoSaldo = ''
                    OR rango_saldo = :rangoSaldo
                )
        )
        """;


    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    public List<AnalisisTasasCdatCorteDTO> listarCortes() {

        String sql = """
            SELECT
                c.fecha_corte,
                c.anio,
                c.mes,

                COUNT(*) AS cantidad_agencias,

                COALESCE(
                    SUM(c.total_cdats),
                    0
                ) AS cantidad_cdats

            FROM cdat.cierres_mensuales_cdats c

            WHERE c.estado = 'GENERADO'

            GROUP BY
                c.fecha_corte,
                c.anio,
                c.mes

            ORDER BY
                c.fecha_corte DESC
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> {

                    AnalisisTasasCdatCorteDTO dto =
                            new AnalisisTasasCdatCorteDTO();

                    dto.setFechaCorte(
                            rs.getObject(
                                    "fecha_corte",
                                    LocalDate.class
                            )
                    );

                    dto.setAnio(
                            rs.getInt("anio")
                    );

                    dto.setMes(
                            rs.getInt("mes")
                    );

                    dto.setCantidadAgencias(
                            rs.getInt(
                                    "cantidad_agencias"
                            )
                    );

                    dto.setCantidadCdats(
                            rs.getInt(
                                    "cantidad_cdats"
                            )
                    );

                    return dto;
                }
        );
    }


    // =========================================================
    // RESUMEN GENERAL
    // =========================================================

    public AnalisisTasasCdatResumenDTO obtenerResumen(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer plazoMeses,
            String amortizacion,
            String rangoSaldo
    ) {

        String sql = BASE_SQL + """
            SELECT
                COUNT(*) AS cantidad_cdats,

                COUNT(
                    DISTINCT id_datos_personal
                ) AS cantidad_depositantes,

                COALESCE(
                    SUM(saldo_actual_cdat),
                    0
                ) AS saldo_total,

                COALESCE(
                    AVG(saldo_actual_cdat),
                    0
                ) AS saldo_promedio,

                COALESCE(
                    SUM(
                        saldo_actual_cdat
                        * tasa_nominal_anual
                    )
                    / NULLIF(
                        SUM(saldo_actual_cdat),
                        0
                    ),
                    0
                ) AS tasa_nominal_ponderada,

                COALESCE(
                    SUM(
                        saldo_actual_cdat
                        * tasa_efectiva_anual
                    )
                    / NULLIF(
                        SUM(saldo_actual_cdat),
                        0
                    ),
                    0
                ) AS tasa_efectiva_ponderada,

                COALESCE(
                    SUM(
                        saldo_actual_cdat
                        * plazo_meses
                    )
                    / NULLIF(
                        SUM(saldo_actual_cdat),
                        0
                    ),
                    0
                ) AS plazo_ponderado_meses

            FROM poblacion
            """;

        return jdbc.queryForObject(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia,
                        plazoMeses,
                        amortizacion,
                        rangoSaldo
                ),
                (rs, rowNum) -> {

                    AnalisisTasasCdatResumenDTO dto =
                            new AnalisisTasasCdatResumenDTO();

                    dto.setCantidadCdats(
                            rs.getInt(
                                    "cantidad_cdats"
                            )
                    );

                    dto.setCantidadDepositantes(
                            rs.getInt(
                                    "cantidad_depositantes"
                            )
                    );

                    dto.setSaldoTotal(
                            rs.getBigDecimal(
                                    "saldo_total"
                            )
                    );

                    dto.setSaldoPromedio(
                            rs.getBigDecimal(
                                    "saldo_promedio"
                            )
                    );

                    dto.setTasaNominalPonderada(
                            rs.getBigDecimal(
                                    "tasa_nominal_ponderada"
                            )
                    );

                    dto.setTasaEfectivaPonderada(
                            rs.getBigDecimal(
                                    "tasa_efectiva_ponderada"
                            )
                    );

                    dto.setPlazoPonderadoMeses(
                            rs.getBigDecimal(
                                    "plazo_ponderado_meses"
                            )
                    );

                    return dto;
                }
        );
    }


    // =========================================================
    // CONDICIONES DE CAPTACIÓN
    // PLAZO × AMORTIZACIÓN
    // =========================================================

    public List<AnalisisTasasCdatCondicionDTO> obtenerCondiciones(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer plazoMeses,
            String amortizacion,
            String rangoSaldo
    ) {

        String sql = BASE_SQL + """
            ,
            total AS (
                SELECT
                    COALESCE(
                        SUM(saldo_actual_cdat),
                        0
                    ) AS saldo_total
                FROM poblacion
            )

            SELECT
                p.plazo_meses,
                p.amortizacion_deposito,
                p.nombre_amortizacion,

                COUNT(*) AS cantidad_cdats,

                COALESCE(
                    SUM(p.saldo_actual_cdat),
                    0
                ) AS saldo_total,

                CASE
                    WHEN t.saldo_total = 0
                        THEN 0
                    ELSE
                        (
                            SUM(p.saldo_actual_cdat)
                            * 100.0
                            / t.saldo_total
                        )
                END AS participacion_saldo,

                COALESCE(
                    SUM(
                        p.saldo_actual_cdat
                        * p.tasa_nominal_anual
                    )
                    / NULLIF(
                        SUM(p.saldo_actual_cdat),
                        0
                    ),
                    0
                ) AS tasa_nominal_ponderada,

                COALESCE(
                    SUM(
                        p.saldo_actual_cdat
                        * p.tasa_efectiva_anual
                    )
                    / NULLIF(
                        SUM(p.saldo_actual_cdat),
                        0
                    ),
                    0
                ) AS tasa_efectiva_ponderada

            FROM poblacion p

            CROSS JOIN total t

            GROUP BY
                p.plazo_meses,
                p.amortizacion_deposito,
                p.nombre_amortizacion,
                t.saldo_total

            ORDER BY
                p.plazo_meses,
                p.amortizacion_deposito
            """;

        return jdbc.query(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia,
                        plazoMeses,
                        amortizacion,
                        rangoSaldo
                ),
                (rs, rowNum) -> {

                    AnalisisTasasCdatCondicionDTO dto =
                            new AnalisisTasasCdatCondicionDTO();

                    dto.setPlazoMeses(
                            rs.getInt(
                                    "plazo_meses"
                            )
                    );

                    dto.setAmortizacionDeposito(
                            rs.getString(
                                    "amortizacion_deposito"
                            )
                    );

                    dto.setNombreAmortizacion(
                            rs.getString(
                                    "nombre_amortizacion"
                            )
                    );

                    dto.setCantidadCdats(
                            rs.getInt(
                                    "cantidad_cdats"
                            )
                    );

                    dto.setSaldoTotal(
                            rs.getBigDecimal(
                                    "saldo_total"
                            )
                    );

                    dto.setParticipacionSaldo(
                            rs.getBigDecimal(
                                    "participacion_saldo"
                            )
                    );

                    dto.setTasaNominalPonderada(
                            rs.getBigDecimal(
                                    "tasa_nominal_ponderada"
                            )
                    );

                    dto.setTasaEfectivaPonderada(
                            rs.getBigDecimal(
                                    "tasa_efectiva_ponderada"
                            )
                    );

                    return dto;
                }
        );
    }


    // =========================================================
    // ANÁLISIS POR RANGO DE SALDO
    // =========================================================

    public List<AnalisisTasasCdatRangoSaldoDTO> obtenerRangosSaldo(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer plazoMeses,
            String amortizacion,
            String rangoSaldo
    ) {

        String sql = BASE_SQL + """
            ,
            total AS (
                SELECT
                    COALESCE(
                        SUM(saldo_actual_cdat),
                        0
                    ) AS saldo_total
                FROM poblacion
            )

            SELECT
                p.rango_saldo,

                COUNT(*) AS cantidad_cdats,

                COALESCE(
                    SUM(p.saldo_actual_cdat),
                    0
                ) AS saldo_total,

                CASE
                    WHEN t.saldo_total = 0
                        THEN 0
                    ELSE
                        (
                            SUM(p.saldo_actual_cdat)
                            * 100.0
                            / t.saldo_total
                        )
                END AS participacion_saldo,

                COALESCE(
                    SUM(
                        p.saldo_actual_cdat
                        * p.tasa_nominal_anual
                    )
                    / NULLIF(
                        SUM(p.saldo_actual_cdat),
                        0
                    ),
                    0
                ) AS tasa_nominal_ponderada,

                COALESCE(
                    SUM(
                        p.saldo_actual_cdat
                        * p.tasa_efectiva_anual
                    )
                    / NULLIF(
                        SUM(p.saldo_actual_cdat),
                        0
                    ),
                    0
                ) AS tasa_efectiva_ponderada,

                COALESCE(
                    MIN(p.tasa_efectiva_anual),
                    0
                ) AS tasa_efectiva_minima,

                COALESCE(
                    MAX(p.tasa_efectiva_anual),
                    0
                ) AS tasa_efectiva_maxima

            FROM poblacion p

            CROSS JOIN total t

            GROUP BY
                p.rango_saldo,
                t.saldo_total

            ORDER BY
                p.rango_saldo
            """;

        return jdbc.query(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia,
                        plazoMeses,
                        amortizacion,
                        rangoSaldo
                ),
                (rs, rowNum) -> {

                    AnalisisTasasCdatRangoSaldoDTO dto =
                            new AnalisisTasasCdatRangoSaldoDTO();

                    dto.setRangoSaldo(
                            rs.getString(
                                    "rango_saldo"
                            )
                    );

                    dto.setCantidadCdats(
                            rs.getInt(
                                    "cantidad_cdats"
                            )
                    );

                    dto.setSaldoTotal(
                            rs.getBigDecimal(
                                    "saldo_total"
                            )
                    );

                    dto.setParticipacionSaldo(
                            rs.getBigDecimal(
                                    "participacion_saldo"
                            )
                    );

                    dto.setTasaNominalPonderada(
                            rs.getBigDecimal(
                                    "tasa_nominal_ponderada"
                            )
                    );

                    dto.setTasaEfectivaPonderada(
                            rs.getBigDecimal(
                                    "tasa_efectiva_ponderada"
                            )
                    );

                    dto.setTasaEfectivaMinima(
                            rs.getBigDecimal(
                                    "tasa_efectiva_minima"
                            )
                    );

                    dto.setTasaEfectivaMaxima(
                            rs.getBigDecimal(
                                    "tasa_efectiva_maxima"
                            )
                    );

                    return dto;
                }
        );
    }


    // =========================================================
    // DETALLE AUDITABLE
    // =========================================================

    public List<AnalisisTasasCdatDetalleDTO> obtenerDetalle(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer plazoMeses,
            String amortizacion,
            String rangoSaldo
    ) {

        String sql = BASE_SQL + """
            SELECT
                id_cuenta_cdat,
                codigo_cdat,

                id_agencia,
                codigo_agencia,
                nombre_agencia,

                id_datos_personal,
                tipo_documento,
                documento,
                nombre_completo,

                fecha_apertura_cdat,
                fecha_vencimiento_cdat,

                plazo_meses,
                plazo_dias,

                amortizacion_deposito,
                nombre_amortizacion,

                valor_apertura_cdat,
                saldo_actual_cdat,

                tasa_nominal_anual,
                tasa_efectiva_anual

            FROM poblacion

            ORDER BY
                saldo_actual_cdat DESC,
                codigo_cdat
            """;

        return jdbc.query(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia,
                        plazoMeses,
                        amortizacion,
                        rangoSaldo
                ),
                (rs, rowNum) -> {

                    AnalisisTasasCdatDetalleDTO dto =
                            new AnalisisTasasCdatDetalleDTO();

                    dto.setIdCuentaCdat(
                            rs.getObject(
                                    "id_cuenta_cdat",
                                    Long.class
                            )
                    );

                    dto.setCodigoCdat(
                            rs.getString(
                                    "codigo_cdat"
                            )
                    );

                    dto.setIdAgencia(
                            rs.getObject(
                                    "id_agencia",
                                    Integer.class
                            )
                    );

                    dto.setCodigoAgencia(
                            rs.getString(
                                    "codigo_agencia"
                            )
                    );

                    dto.setNombreAgencia(
                            rs.getString(
                                    "nombre_agencia"
                            )
                    );

                    dto.setIdDatosPersonal(
                            rs.getObject(
                                    "id_datos_personal",
                                    Integer.class
                            )
                    );

                    dto.setTipoDocumento(
                            rs.getString(
                                    "tipo_documento"
                            )
                    );

                    dto.setDocumento(
                            rs.getString(
                                    "documento"
                            )
                    );

                    dto.setNombreCompleto(
                            rs.getString(
                                    "nombre_completo"
                            )
                    );

                    dto.setFechaAperturaCdat(
                            rs.getObject(
                                    "fecha_apertura_cdat",
                                    LocalDate.class
                            )
                    );

                    dto.setFechaVencimientoCdat(
                            rs.getObject(
                                    "fecha_vencimiento_cdat",
                                    LocalDate.class
                            )
                    );

                    dto.setPlazoMeses(
                            rs.getObject(
                                    "plazo_meses",
                                    Integer.class
                            )
                    );

                    dto.setPlazoDias(
                            rs.getObject(
                                    "plazo_dias",
                                    Integer.class
                            )
                    );

                    dto.setAmortizacionDeposito(
                            rs.getString(
                                    "amortizacion_deposito"
                            )
                    );

                    dto.setNombreAmortizacion(
                            rs.getString(
                                    "nombre_amortizacion"
                            )
                    );

                    dto.setValorAperturaCdat(
                            rs.getBigDecimal(
                                    "valor_apertura_cdat"
                            )
                    );

                    dto.setSaldoActualCdat(
                            rs.getBigDecimal(
                                    "saldo_actual_cdat"
                            )
                    );

                    dto.setTasaNominalAnual(
                            rs.getBigDecimal(
                                    "tasa_nominal_anual"
                            )
                    );

                    dto.setTasaEfectivaAnual(
                            rs.getBigDecimal(
                                    "tasa_efectiva_anual"
                            )
                    );

                    return dto;
                }
        );
    }


    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource parametros(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer plazoMeses,
            String amortizacion,
            String rangoSaldo
    ) {

        return new MapSqlParameterSource()

                .addValue(
                        "fechaCorte",
                        fechaCorte == null
                                ? null
                                : Date.valueOf(fechaCorte),
                        Types.DATE
                )

                .addValue(
                        "idAgencia",
                        idAgencia,
                        Types.INTEGER
                )

                .addValue(
                        "plazoMeses",
                        plazoMeses,
                        Types.INTEGER
                )

                .addValue(
                        "amortizacion",
                        amortizacion,
                        Types.VARCHAR
                )

                .addValue(
                        "rangoSaldo",
                        rangoSaldo,
                        Types.VARCHAR
                );
    }

}
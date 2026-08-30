package co.assip.erp.cartera.analisis.moratemprana;

import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaCosechaDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaDetalleDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaPrimeraMoraDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaResumenDTO;
import co.assip.erp.cartera.analisis.moratemprana.dto.MoraTempranaSegmentoDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AnalisisMoraTempranaRepository {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<MoraTempranaResumenDTO>
            RESUMEN_MAPPER =
            crearMapper(MoraTempranaResumenDTO.class);

    private static final BeanPropertyRowMapper<MoraTempranaCosechaDTO>
            COSECHA_MAPPER =
            crearMapper(MoraTempranaCosechaDTO.class);

    private static final BeanPropertyRowMapper<MoraTempranaSegmentoDTO>
            SEGMENTO_MAPPER =
            crearMapper(MoraTempranaSegmentoDTO.class);

    private static final BeanPropertyRowMapper<MoraTempranaPrimeraMoraDTO>
            PRIMERA_MORA_MAPPER =
            crearMapper(MoraTempranaPrimeraMoraDTO.class);

    private static final BeanPropertyRowMapper<MoraTempranaDetalleDTO>
            DETALLE_MAPPER =
            crearMapper(MoraTempranaDetalleDTO.class);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AnalisisMoraTempranaRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // FECHA MÁXIMA DISPONIBLE
    // =========================================================

    public Optional<LocalDate> obtenerUltimoCorteDisponible() {

        String sql = """
                SELECT MAX(h.fecha_corte)
                FROM cartera.vw_cartera_resultados_mensuales_total h
                WHERE h.fecha_corte IS NOT NULL
                """;

        LocalDate fecha = jdbc.getJdbcTemplate().queryForObject(
                sql,
                LocalDate.class
        );

        return Optional.ofNullable(fecha);
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    public Optional<MoraTempranaResumenDTO> consultarResumen(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = cteBase() + """
                SELECT
                    MIN(e.cosecha) AS cosecha_desde,
                    MAX(e.cosecha) AS cosecha_hasta,

                    COUNT(DISTINCT e.cosecha)::integer
                        AS cantidad_cosechas,

                    COUNT(*)::integer
                        AS creditos_originados,

                    COALESCE(
                        SUM(e.valor_desembolsado),
                        0
                    ) AS valor_desembolsado,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob30 <= 3
                    )::integer AS mora30_hasta_mob3,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob30 <= 3
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora30_mob3,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob30 <= 6
                    )::integer AS mora30_hasta_mob6,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob30 <= 6
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora30_mob6,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob60 <= 6
                    )::integer AS mora60_hasta_mob6,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob60 <= 6
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora60_mob6

                FROM eventos e
                """;

        List<MoraTempranaResumenDTO> datos =
                jdbc.query(
                        sql,
                        parametros(
                                cosechaDesde,
                                cosechaHasta,
                                idAgencia,
                                idLineaCredito
                        ),
                        RESUMEN_MAPPER
                );

        if (datos.isEmpty()) {
            return Optional.empty();
        }

        MoraTempranaResumenDTO dto = datos.get(0);

        if (dto.getCantidadCosechas() == null
                || dto.getCantidadCosechas() == 0) {
            return Optional.empty();
        }

        return Optional.of(dto);
    }

    // =========================================================
    // ANÁLISIS POR COSECHA
    // =========================================================

    public List<MoraTempranaCosechaDTO> consultarCosechas(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = cteBase() + """
                SELECT
                    e.cosecha,

                    COUNT(*)::integer
                        AS creditos_originados,

                    COALESCE(
                        SUM(e.valor_desembolsado),
                        0
                    ) AS valor_desembolsado,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob30 <= 3
                    )::integer AS mora30_hasta_mob3,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob30 <= 3
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora30_mob3,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob30 <= 6
                    )::integer AS mora30_hasta_mob6,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob30 <= 6
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora30_mob6,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob60 <= 6
                    )::integer AS mora60_hasta_mob6,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob60 <= 6
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora60_mob6

                FROM eventos e

                GROUP BY
                    e.cosecha

                ORDER BY
                    e.cosecha
                """;

        return jdbc.query(
                sql,
                parametros(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                ),
                COSECHA_MAPPER
        );
    }

    // =========================================================
    // ANÁLISIS POR AGENCIA
    // =========================================================

    public List<MoraTempranaSegmentoDTO> consultarPorAgencia(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = cteBase() + """
                SELECT
                    e.id_agencia AS id,

                    COALESCE(
                        a.codigo_agencia,
                        ''
                    ) AS codigo,

                    COALESCE(
                        a.nombre_agencia,
                        'SIN AGENCIA'
                    ) AS descripcion,

                    COUNT(*)::integer
                        AS creditos_originados,

                    COALESCE(
                        SUM(e.valor_desembolsado),
                        0
                    ) AS valor_desembolsado,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob30 <= 3
                    )::integer AS mora30_hasta_mob3,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob30 <= 3
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora30_mob3,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob30 <= 6
                    )::integer AS mora30_hasta_mob6,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob30 <= 6
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora30_mob6,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob60 <= 6
                    )::integer AS mora60_hasta_mob6,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob60 <= 6
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora60_mob6

                FROM eventos e

                LEFT JOIN general.datos_agencias a
                       ON a.id_agencia = e.id_agencia

                GROUP BY
                    e.id_agencia,
                    a.codigo_agencia,
                    a.nombre_agencia

                ORDER BY
                    porcentaje_mora30_mob6 DESC,
                    creditos_originados DESC,
                    descripcion
                """;

        return jdbc.query(
                sql,
                parametros(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                ),
                SEGMENTO_MAPPER
        );
    }

    // =========================================================
    // ANÁLISIS POR LÍNEA
    // =========================================================

    public List<MoraTempranaSegmentoDTO> consultarPorLinea(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = cteBase() + """
                SELECT
                    e.id_linea_credito AS id,

                    COALESCE(
                        e.codigo_linea_credito,
                        ''
                    ) AS codigo,

                    COALESCE(
                        e.nombre_linea_credito,
                        'SIN LÍNEA'
                    ) AS descripcion,

                    COUNT(*)::integer
                        AS creditos_originados,

                    COALESCE(
                        SUM(e.valor_desembolsado),
                        0
                    ) AS valor_desembolsado,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob30 <= 3
                    )::integer AS mora30_hasta_mob3,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob30 <= 3
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora30_mob3,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob30 <= 6
                    )::integer AS mora30_hasta_mob6,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob30 <= 6
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora30_mob6,

                    COUNT(*) FILTER (
                        WHERE e.primer_mob60 <= 6
                    )::integer AS mora60_hasta_mob6,

                    ROUND(
                        100.0
                        * COUNT(*) FILTER (
                            WHERE e.primer_mob60 <= 6
                        )
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS porcentaje_mora60_mob6

                FROM eventos e

                GROUP BY
                    e.id_linea_credito,
                    e.codigo_linea_credito,
                    e.nombre_linea_credito

                ORDER BY
                    porcentaje_mora30_mob6 DESC,
                    creditos_originados DESC,
                    descripcion
                """;

        return jdbc.query(
                sql,
                parametros(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                ),
                SEGMENTO_MAPPER
        );
    }

    // =========================================================
    // PRIMERA MORA
    // =========================================================

    public List<MoraTempranaPrimeraMoraDTO> consultarPrimeraMora(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = cteBase() + """
                , mobs AS (
                    SELECT generate_series(1, 6)::integer AS mob
                ),

                eventos_primera_mora AS (
                    SELECT
                        '30+'::varchar AS evento,
                        m.mob,
                        COUNT(e.id_cartera_credito) FILTER (
                            WHERE e.primer_mob30 = m.mob
                        )::integer AS cantidad_creditos

                    FROM mobs m
                    CROSS JOIN eventos e

                    GROUP BY m.mob

                    UNION ALL

                    SELECT
                        '60+'::varchar AS evento,
                        m.mob,
                        COUNT(e.id_cartera_credito) FILTER (
                            WHERE e.primer_mob60 = m.mob
                        )::integer AS cantidad_creditos

                    FROM mobs m
                    CROSS JOIN eventos e

                    GROUP BY m.mob
                ),

                total AS (
                    SELECT COUNT(*)::numeric AS cantidad
                    FROM eventos
                )

                SELECT
                    p.evento,
                    p.mob,
                    p.cantidad_creditos,

                    ROUND(
                        100.0
                        * p.cantidad_creditos
                        / NULLIF(t.cantidad, 0),
                        4
                    ) AS porcentaje_creditos

                FROM eventos_primera_mora p
                CROSS JOIN total t

                ORDER BY
                    CASE p.evento
                        WHEN '30+' THEN 1
                        WHEN '60+' THEN 2
                        ELSE 9
                    END,
                    p.mob
                """;

        return jdbc.query(
                sql,
                parametros(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                ),
                PRIMERA_MORA_MAPPER
        );
    }

    // =========================================================
    // DETALLE AUDITABLE
    // =========================================================

    public List<MoraTempranaDetalleDTO> consultarDetalle(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito,
            String indicador
    ) {

        String sql = cteBaseDetalle() + """
                SELECT
                    e.id_cartera_credito,
                    e.id_agencia,

                    COALESCE(
                        a.codigo_agencia,
                        ''
                    ) AS codigo_agencia,

                    COALESCE(
                        a.nombre_agencia,
                        'SIN AGENCIA'
                    ) AS nombre_agencia,

                    e.id_linea_credito,
                    e.codigo_linea_credito,
                    e.nombre_linea_credito,
                    e.pagare_cartera,

                    e.id_datos_personal,
                    e.tipo_documento,
                    e.documento,
                    e.nombre_completo,

                    e.cosecha,
                    e.fecha_desembolso,
                    e.valor_inicial_credito,
                    e.valor_desembolsado,

                    e.primer_mob30,
                    e.primer_mob60,
                    e.max_dias_mora_hasta_mob6,

                    (e.primer_mob30 <= 3)
                        AS mora30_hasta_mob3,

                    (e.primer_mob30 <= 6)
                        AS mora30_hasta_mob6,

                    (e.primer_mob60 <= 6)
                        AS mora60_hasta_mob6,

                    u.fecha_corte
                        AS fecha_ultimo_corte_observado,

                    u.mob
                        AS mob_ultimo_corte_observado,

                    u.saldo_credito_fecha_corte
                        AS saldo_ultimo_corte_observado,

                    u.dias_mora
                        AS dias_mora_ultimo_corte_observado,

                    dp.telefono
                        AS telefono,

                    dp.celular_uno
                        AS celular,

                    dp.correo_personal
                        AS correo

                FROM eventos e

                LEFT JOIN ultima_observacion u
                       ON u.id_cartera_credito =
                          e.id_cartera_credito

                LEFT JOIN general.datos_agencias a
                       ON a.id_agencia =
                          e.id_agencia

                LEFT JOIN reporting.vw_datos_personales_operativa dp
                       ON dp.id_datos_personal =
                          e.id_datos_personal

                WHERE
                    (
                        CAST(:indicador AS varchar) IS NULL
                        OR CAST(:indicador AS varchar) = 'TODOS'

                        OR (
                            CAST(:indicador AS varchar) = 'MORA30_MOB3'
                            AND e.primer_mob30 <= 3
                        )

                        OR (
                            CAST(:indicador AS varchar) = 'MORA30_MOB6'
                            AND e.primer_mob30 <= 6
                        )

                        OR (
                            CAST(:indicador AS varchar) = 'MORA60_MOB6'
                            AND e.primer_mob60 <= 6
                        )
                    )

                ORDER BY
                    e.cosecha,
                    e.id_agencia,
                    e.id_linea_credito,
                    e.documento,
                    e.pagare_cartera
                """;

        MapSqlParameterSource params =
                parametros(
                        cosechaDesde,
                        cosechaHasta,
                        idAgencia,
                        idLineaCredito
                )
                        .addValue(
                                "indicador",
                                normalizarIndicador(indicador)
                        );

        return jdbc.query(
                sql,
                params,
                DETALLE_MAPPER
        );
    }

    // =========================================================
    // CTE BASE
    // =========================================================

    private String cteBase() {

        return """
            WITH ultimo_corte AS MATERIALIZED (
                SELECT
                    MAX(h.fecha_corte) AS fecha_corte
                FROM cartera.vw_cartera_resultados_mensuales_total h
                WHERE h.fecha_corte IS NOT NULL
            ),

            poblacion AS MATERIALIZED (
                SELECT DISTINCT ON (h.id_cartera_credito)
                    h.id_cartera_credito,
                    h.id_agencia,
                    h.id_linea_credito,
                    h.codigo_linea_credito,
                    h.nombre_linea_credito,

                    DATE_TRUNC(
                        'month',
                        h.fecha_desembolso
                    )::date AS cosecha,

                    h.fecha_desembolso,
                    h.valor_desembolsado

                FROM cartera.vw_cartera_resultados_mensuales_total h
                CROSS JOIN ultimo_corte uc

                WHERE h.fecha_desembolso IS NOT NULL

                  AND h.fecha_desembolso >=
                      CAST(:cosechaDesde AS date)

                  AND h.fecha_desembolso <
                      CAST(:cosechaHastaExclusiva AS date)

                  AND (
                      CAST(:idAgencia AS integer) IS NULL
                      OR h.id_agencia =
                         CAST(:idAgencia AS integer)
                  )

                  AND (
                      CAST(:idLineaCredito AS integer) IS NULL
                      OR h.id_linea_credito =
                         CAST(:idLineaCredito AS integer)
                  )

                  /*
                   * Solo cosechas que ya alcanzaron MOB 6.
                   */
                  AND DATE_TRUNC(
                          'month',
                          h.fecha_desembolso
                      )::date
                      <= (
                          DATE_TRUNC(
                              'month',
                              uc.fecha_corte
                          )
                          - INTERVAL '6 months'
                      )::date

                ORDER BY
                    h.id_cartera_credito,
                    h.fecha_corte
            ),

            historia AS MATERIALIZED (
                SELECT
                    p.id_cartera_credito,
                    h.fecha_corte,

                    COALESCE(
                        h.dias_mora,
                        0
                    ) AS dias_mora,

                    COALESCE(
                        h.saldo_credito_fecha_corte,
                        0
                    ) AS saldo_credito_fecha_corte,

                    (
                        EXTRACT(YEAR FROM h.fecha_corte)::int * 12
                        + EXTRACT(MONTH FROM h.fecha_corte)::int
                    )
                    -
                    (
                        EXTRACT(YEAR FROM p.cosecha)::int * 12
                        + EXTRACT(MONTH FROM p.cosecha)::int
                    ) AS mob

                FROM poblacion p

                JOIN cartera.vw_cartera_resultados_mensuales_total h
                  ON h.id_cartera_credito =
                     p.id_cartera_credito

                 /*
                  * Ayuda a PostgreSQL a reducir directamente
                  * la vista histórica al rango de desembolsos
                  * que estamos analizando.
                  */
                 AND h.fecha_desembolso >=
                     CAST(:cosechaDesde AS date)

                 AND h.fecha_desembolso <
                     CAST(:cosechaHastaExclusiva AS date)

                 /*
                  * Solo necesitamos fotografías MOB 1 a MOB 6.
                  *
                  * No recorremos toda la historia del crédito.
                  */
                 AND h.fecha_corte >=
                     (
                         p.cosecha
                         + INTERVAL '1 month'
                     )

                 AND h.fecha_corte <
                     (
                         p.cosecha
                         + INTERVAL '7 months'
                     )
            ),

            eventos AS MATERIALIZED (
                SELECT
                    p.id_cartera_credito,
                    p.id_agencia,
                    p.id_linea_credito,
                    p.codigo_linea_credito,
                    p.nombre_linea_credito,
                    p.cosecha,
                    p.fecha_desembolso,
                    p.valor_desembolsado,

                    MIN(h.mob) FILTER (
                        WHERE h.dias_mora >= 30
                          AND h.mob BETWEEN 1 AND 6
                    ) AS primer_mob30,

                    MIN(h.mob) FILTER (
                        WHERE h.dias_mora >= 60
                          AND h.mob BETWEEN 1 AND 6
                    ) AS primer_mob60

                FROM poblacion p

                LEFT JOIN historia h
                       ON h.id_cartera_credito =
                          p.id_cartera_credito

                GROUP BY
                    p.id_cartera_credito,
                    p.id_agencia,
                    p.id_linea_credito,
                    p.codigo_linea_credito,
                    p.nombre_linea_credito,
                    p.cosecha,
                    p.fecha_desembolso,
                    p.valor_desembolsado
            )
            """;
    }

    // =========================================================
    // CTE BASE DEL DETALLE
    // =========================================================

    private String cteBaseDetalle() {

        return """
            WITH ultimo_corte AS MATERIALIZED (
                SELECT
                    MAX(h.fecha_corte) AS fecha_corte
                FROM cartera.vw_cartera_resultados_mensuales_total h
                WHERE h.fecha_corte IS NOT NULL
            ),

            poblacion AS MATERIALIZED (
                SELECT DISTINCT ON (h.id_cartera_credito)
                    h.id_cartera_credito,
                    h.id_agencia,
                    h.id_linea_credito,
                    h.codigo_linea_credito,
                    h.nombre_linea_credito,
                    h.pagare_cartera,
                    h.id_datos_personal,
                    h.tipo_documento,
                    h.documento,
                    h.nombre_completo,

                    DATE_TRUNC(
                        'month',
                        h.fecha_desembolso
                    )::date AS cosecha,

                    h.fecha_desembolso,
                    h.valor_inicial_credito,
                    h.valor_desembolsado

                FROM cartera.vw_cartera_resultados_mensuales_total h
                CROSS JOIN ultimo_corte uc

                WHERE h.fecha_desembolso IS NOT NULL

                  AND h.fecha_desembolso >=
                      CAST(:cosechaDesde AS date)

                  AND h.fecha_desembolso <
                      CAST(:cosechaHastaExclusiva AS date)

                  AND (
                      CAST(:idAgencia AS integer) IS NULL
                      OR h.id_agencia =
                         CAST(:idAgencia AS integer)
                  )

                  AND (
                      CAST(:idLineaCredito AS integer) IS NULL
                      OR h.id_linea_credito =
                         CAST(:idLineaCredito AS integer)
                  )

                  /*
                   * Solamente cosechas que ya alcanzaron MOB 6.
                   */
                  AND DATE_TRUNC(
                          'month',
                          h.fecha_desembolso
                      )::date
                      <= (
                          DATE_TRUNC(
                              'month',
                              uc.fecha_corte
                          )
                          - INTERVAL '6 months'
                      )::date

                ORDER BY
                    h.id_cartera_credito,
                    h.fecha_corte
            ),

            historia AS MATERIALIZED (
                SELECT
                    p.id_cartera_credito,
                    h.fecha_corte,

                    COALESCE(
                        h.dias_mora,
                        0
                    ) AS dias_mora,

                    COALESCE(
                        h.saldo_credito_fecha_corte,
                        0
                    ) AS saldo_credito_fecha_corte,

                    (
                        EXTRACT(YEAR FROM h.fecha_corte)::int * 12
                        + EXTRACT(MONTH FROM h.fecha_corte)::int
                    )
                    -
                    (
                        EXTRACT(YEAR FROM p.cosecha)::int * 12
                        + EXTRACT(MONTH FROM p.cosecha)::int
                    ) AS mob

                FROM poblacion p

                JOIN cartera.vw_cartera_resultados_mensuales_total h
                  ON h.id_cartera_credito =
                     p.id_cartera_credito

                 AND h.fecha_desembolso >=
                     CAST(:cosechaDesde AS date)

                 AND h.fecha_desembolso <
                     CAST(:cosechaHastaExclusiva AS date)

                 /*
                  * Solamente fotografías comprendidas
                  * entre MOB 1 y MOB 6.
                  */
                 AND h.fecha_corte >=
                     (
                         p.cosecha
                         + INTERVAL '1 month'
                     )

                 AND h.fecha_corte <
                     (
                         p.cosecha
                         + INTERVAL '7 months'
                     )
            ),

            eventos AS MATERIALIZED (
                SELECT
                    p.id_cartera_credito,
                    p.id_agencia,
                    p.id_linea_credito,
                    p.codigo_linea_credito,
                    p.nombre_linea_credito,
                    p.pagare_cartera,
                    p.id_datos_personal,
                    p.tipo_documento,
                    p.documento,
                    p.nombre_completo,
                    p.cosecha,
                    p.fecha_desembolso,
                    p.valor_inicial_credito,
                    p.valor_desembolsado,

                    MIN(h.mob) FILTER (
                        WHERE h.dias_mora >= 30
                          AND h.mob BETWEEN 1 AND 6
                    ) AS primer_mob30,

                    MIN(h.mob) FILTER (
                        WHERE h.dias_mora >= 60
                          AND h.mob BETWEEN 1 AND 6
                    ) AS primer_mob60,

                    COALESCE(
                        MAX(h.dias_mora) FILTER (
                            WHERE h.mob BETWEEN 1 AND 6
                        ),
                        0
                    ) AS max_dias_mora_hasta_mob6

                FROM poblacion p

                LEFT JOIN historia h
                       ON h.id_cartera_credito =
                          p.id_cartera_credito

                GROUP BY
                    p.id_cartera_credito,
                    p.id_agencia,
                    p.id_linea_credito,
                    p.codigo_linea_credito,
                    p.nombre_linea_credito,
                    p.pagare_cartera,
                    p.id_datos_personal,
                    p.tipo_documento,
                    p.documento,
                    p.nombre_completo,
                    p.cosecha,
                    p.fecha_desembolso,
                    p.valor_inicial_credito,
                    p.valor_desembolsado
            ),

            ultima_observacion AS MATERIALIZED (
                SELECT DISTINCT ON (
                    h.id_cartera_credito
                )
                    h.id_cartera_credito,
                    h.fecha_corte,
                    h.mob,
                    h.saldo_credito_fecha_corte,
                    h.dias_mora

                FROM historia h

                WHERE h.mob BETWEEN 1 AND 6

                ORDER BY
                    h.id_cartera_credito,
                    h.mob DESC,
                    h.fecha_corte DESC
            )
            """;
    }

    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource parametros(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        LocalDate cosechaHastaExclusiva =
                cosechaHasta
                        .withDayOfMonth(1)
                        .plusMonths(1);

        return new MapSqlParameterSource()
                .addValue(
                        "cosechaDesde",
                        cosechaDesde
                )
                .addValue(
                        "cosechaHasta",
                        cosechaHasta
                )
                .addValue(
                        "cosechaHastaExclusiva",
                        cosechaHastaExclusiva
                )
                .addValue(
                        "idAgencia",
                        idAgencia
                )
                .addValue(
                        "idLineaCredito",
                        idLineaCredito
                );
    }

    // =========================================================
    // NORMALIZACIÓN INDICADOR
    // =========================================================

    private String normalizarIndicador(
            String indicador
    ) {

        if (indicador == null
                || indicador.isBlank()) {
            return "TODOS";
        }

        return indicador
                .trim()
                .toUpperCase();
    }

    // =========================================================
    // MAPPER
    // =========================================================

    private static <T> BeanPropertyRowMapper<T> crearMapper(
            Class<T> tipo
    ) {

        BeanPropertyRowMapper<T> mapper =
                new BeanPropertyRowMapper<>(tipo);

        mapper.setPrimitivesDefaultedForNullValue(true);

        return mapper;
    }
}
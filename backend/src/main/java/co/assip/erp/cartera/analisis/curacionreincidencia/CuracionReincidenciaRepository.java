package co.assip.erp.cartera.analisis.curacionreincidencia;

import co.assip.erp.cartera.analisis.curacionreincidencia.dto.*;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.sql.Types;

@Repository
public class CuracionReincidenciaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CuracionReincidenciaRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public CuracionReincidenciaControlDTO control() {

        String sql = """
                SELECT
                    MIN(h.fecha_corte)::date AS primer_corte_disponible,
                    MAX(h.fecha_corte)::date AS ultimo_corte_disponible
                FROM cartera.vw_cartera_resultados_mensuales_total h
                WHERE h.edad_contable_resultado IN ('A','B','C','D','E')
                """;

        List<CuracionReincidenciaControlDTO> lista =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        new BeanPropertyRowMapper<>(
                                CuracionReincidenciaControlDTO.class
                        )
                );

        return lista.isEmpty() ? null : lista.get(0);
    }

    public CuracionReincidenciaResumenDTO resumen(
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {

        String sql = cteMotor() + """
                SELECT
                    COUNT(*)::int AS episodios,

                    COUNT(*) FILTER (
                        WHERE e.fecha_cura IS NOT NULL
                    )::int AS episodios_curados,

                    COUNT(*) FILTER (
                        WHERE e.fecha_cura IS NULL
                    )::int AS episodios_abiertos,

                    ROUND(
                        COUNT(*) FILTER (
                            WHERE e.fecha_cura IS NOT NULL
                        )::numeric
                        * 100
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS tasa_cura_observada,

                    ROUND(
                        AVG(e.meses_hasta_cura)
                        FILTER (
                            WHERE e.fecha_cura IS NOT NULL
                        ),
                        2
                    ) AS meses_promedio_cura,

                    COUNT(*) FILTER (
                        WHERE e.seguimiento6m_completo
                    )::int AS curas_seguimiento6m,

                    COUNT(*) FILTER (
                        WHERE e.seguimiento6m_completo
                          AND e.fecha_reincidencia IS NOT NULL
                    )::int AS reincidentes6m,

                    ROUND(
                        COUNT(*) FILTER (
                            WHERE e.seguimiento6m_completo
                              AND e.fecha_reincidencia IS NOT NULL
                        )::numeric
                        * 100
                        /
                        NULLIF(
                            COUNT(*) FILTER (
                                WHERE e.seguimiento6m_completo
                            ),
                            0
                        ),
                        4
                    ) AS tasa_reincidencia6m,

                    ROUND(
                        AVG(e.meses_hasta_reincidencia)
                        FILTER (
                            WHERE e.seguimiento6m_completo
                              AND e.fecha_reincidencia IS NOT NULL
                        ),
                        2
                    ) AS meses_promedio_reincidencia

                FROM episodios_filtrados e
                """;

        List<CuracionReincidenciaResumenDTO> lista =
                jdbc.query(
                        sql,
                        parametros(
                                periodoDesde,
                                periodoHastaExclusivo,
                                idAgencia,
                                idLineaCredito,
                                edadEntrada
                        ),
                        new BeanPropertyRowMapper<>(
                                CuracionReincidenciaResumenDTO.class
                        )
                );

        return lista.isEmpty()
                ? new CuracionReincidenciaResumenDTO()
                : lista.get(0);
    }

    public List<CuracionReincidenciaPeriodoDTO> periodos(
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {

        String sql = cteMotor() + """
                SELECT
                    e.periodo_inicio,

                    COUNT(*)::int AS episodios,

                    COUNT(*) FILTER (
                        WHERE e.fecha_cura IS NOT NULL
                    )::int AS episodios_curados,

                    COUNT(*) FILTER (
                        WHERE e.fecha_cura IS NULL
                    )::int AS episodios_abiertos,

                    ROUND(
                        COUNT(*) FILTER (
                            WHERE e.fecha_cura IS NOT NULL
                        )::numeric
                        * 100
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS tasa_cura_observada,

                    ROUND(
                        AVG(e.meses_hasta_cura)
                        FILTER (
                            WHERE e.fecha_cura IS NOT NULL
                        ),
                        2
                    ) AS meses_promedio_cura,

                    COUNT(*) FILTER (
                        WHERE e.seguimiento6m_completo
                    )::int AS curas_seguimiento6m,

                    COUNT(*) FILTER (
                        WHERE e.seguimiento6m_completo
                          AND e.fecha_reincidencia IS NOT NULL
                    )::int AS reincidentes6m,

                    ROUND(
                        COUNT(*) FILTER (
                            WHERE e.seguimiento6m_completo
                              AND e.fecha_reincidencia IS NOT NULL
                        )::numeric
                        * 100
                        /
                        NULLIF(
                            COUNT(*) FILTER (
                                WHERE e.seguimiento6m_completo
                            ),
                            0
                        ),
                        4
                    ) AS tasa_reincidencia6m,

                    ROUND(
                        AVG(e.meses_hasta_reincidencia)
                        FILTER (
                            WHERE e.seguimiento6m_completo
                              AND e.fecha_reincidencia IS NOT NULL
                        ),
                        2
                    ) AS meses_promedio_reincidencia

                FROM episodios_filtrados e

                GROUP BY e.periodo_inicio

                ORDER BY e.periodo_inicio
                """;

        return jdbc.query(
                sql,
                parametros(
                        periodoDesde,
                        periodoHastaExclusivo,
                        idAgencia,
                        idLineaCredito,
                        edadEntrada
                ),
                new BeanPropertyRowMapper<>(
                        CuracionReincidenciaPeriodoDTO.class
                )
        );
    }

    public List<CuracionReincidenciaSegmentoDTO> agencias(
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        return segmentos(
                "e.id_agencia",
                "e.codigo_agencia",
                "e.nombre_agencia",
                periodoDesde,
                periodoHastaExclusivo,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    public List<CuracionReincidenciaSegmentoDTO> lineas(
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        return segmentos(
                "e.id_linea_credito",
                "e.codigo_linea_credito",
                "e.nombre_linea_credito",
                periodoDesde,
                periodoHastaExclusivo,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    private List<CuracionReincidenciaSegmentoDTO> segmentos(
            String idSql,
            String codigoSql,
            String descripcionSql,
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {

        String sql = cteMotor() + """
                SELECT
                    %s AS id,
                    %s AS codigo,
                    %s AS descripcion,

                    COUNT(*)::int AS episodios,

                    COUNT(*) FILTER (
                        WHERE e.fecha_cura IS NOT NULL
                    )::int AS episodios_curados,

                    COUNT(*) FILTER (
                        WHERE e.fecha_cura IS NULL
                    )::int AS episodios_abiertos,

                    ROUND(
                        COUNT(*) FILTER (
                            WHERE e.fecha_cura IS NOT NULL
                        )::numeric
                        * 100
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS tasa_cura_observada,

                    ROUND(
                        AVG(e.meses_hasta_cura)
                        FILTER (
                            WHERE e.fecha_cura IS NOT NULL
                        ),
                        2
                    ) AS meses_promedio_cura,

                    COUNT(*) FILTER (
                        WHERE e.seguimiento6m_completo
                    )::int AS curas_seguimiento6m,

                    COUNT(*) FILTER (
                        WHERE e.seguimiento6m_completo
                          AND e.fecha_reincidencia IS NOT NULL
                    )::int AS reincidentes6m,

                    ROUND(
                        COUNT(*) FILTER (
                            WHERE e.seguimiento6m_completo
                              AND e.fecha_reincidencia IS NOT NULL
                        )::numeric
                        * 100
                        /
                        NULLIF(
                            COUNT(*) FILTER (
                                WHERE e.seguimiento6m_completo
                            ),
                            0
                        ),
                        4
                    ) AS tasa_reincidencia6m,

                    ROUND(
                        AVG(e.meses_hasta_reincidencia)
                        FILTER (
                            WHERE e.seguimiento6m_completo
                              AND e.fecha_reincidencia IS NOT NULL
                        ),
                        2
                    ) AS meses_promedio_reincidencia

                FROM episodios_filtrados e

                GROUP BY
                    %s,
                    %s,
                    %s

                ORDER BY
                    tasa_reincidencia6m DESC NULLS LAST,
                    episodios DESC,
                    descripcion
                """.formatted(
                idSql,
                codigoSql,
                descripcionSql,
                idSql,
                codigoSql,
                descripcionSql
        );

        return jdbc.query(
                sql,
                parametros(
                        periodoDesde,
                        periodoHastaExclusivo,
                        idAgencia,
                        idLineaCredito,
                        edadEntrada
                ),
                new BeanPropertyRowMapper<>(
                        CuracionReincidenciaSegmentoDTO.class
                )
        );
    }

    public List<CuracionReincidenciaEdadEntradaDTO> edadesEntrada(
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {

        String sql = cteMotor() + """
                SELECT
                    e.edad_entrada,

                    COUNT(*)::int AS episodios,

                    COUNT(*) FILTER (
                        WHERE e.fecha_cura IS NOT NULL
                    )::int AS episodios_curados,

                    COUNT(*) FILTER (
                        WHERE e.fecha_cura IS NULL
                    )::int AS episodios_abiertos,

                    ROUND(
                        COUNT(*) FILTER (
                            WHERE e.fecha_cura IS NOT NULL
                        )::numeric
                        * 100
                        / NULLIF(COUNT(*), 0),
                        4
                    ) AS tasa_cura_observada,

                    ROUND(
                        AVG(e.meses_hasta_cura)
                        FILTER (
                            WHERE e.fecha_cura IS NOT NULL
                        ),
                        2
                    ) AS meses_promedio_cura,

                    COUNT(*) FILTER (
                        WHERE e.seguimiento6m_completo
                    )::int AS curas_seguimiento6m,

                    COUNT(*) FILTER (
                        WHERE e.seguimiento6m_completo
                          AND e.fecha_reincidencia IS NOT NULL
                    )::int AS reincidentes6m,

                    ROUND(
                        COUNT(*) FILTER (
                            WHERE e.seguimiento6m_completo
                              AND e.fecha_reincidencia IS NOT NULL
                        )::numeric
                        * 100
                        /
                        NULLIF(
                            COUNT(*) FILTER (
                                WHERE e.seguimiento6m_completo
                            ),
                            0
                        ),
                        4
                    ) AS tasa_reincidencia6m,

                    ROUND(
                        AVG(e.meses_hasta_reincidencia)
                        FILTER (
                            WHERE e.seguimiento6m_completo
                              AND e.fecha_reincidencia IS NOT NULL
                        ),
                        2
                    ) AS meses_promedio_reincidencia

                FROM episodios_filtrados e

                GROUP BY e.edad_entrada

                ORDER BY
                    CASE e.edad_entrada
                        WHEN 'B' THEN 1
                        WHEN 'C' THEN 2
                        WHEN 'D' THEN 3
                        WHEN 'E' THEN 4
                        ELSE 5
                    END
                """;

        return jdbc.query(
                sql,
                parametros(
                        periodoDesde,
                        periodoHastaExclusivo,
                        idAgencia,
                        idLineaCredito,
                        edadEntrada
                ),
                new BeanPropertyRowMapper<>(
                        CuracionReincidenciaEdadEntradaDTO.class
                )
        );
    }

    public List<CuracionReincidenciaDistribucionCuraDTO> distribucionCura(
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {

        String sql = cteMotor() + """
                , distribucion AS (
                    SELECT
                        CASE
                            WHEN e.fecha_cura IS NULL
                                THEN 6
                            WHEN e.meses_hasta_cura = 1
                                THEN 1
                            WHEN e.meses_hasta_cura = 2
                                THEN 2
                            WHEN e.meses_hasta_cura = 3
                                THEN 3
                            WHEN e.meses_hasta_cura BETWEEN 4 AND 6
                                THEN 4
                            ELSE 5
                        END AS orden,

                        CASE
                            WHEN e.fecha_cura IS NULL
                                THEN 'SIN_CURAR'
                            WHEN e.meses_hasta_cura = 1
                                THEN '1_MES'
                            WHEN e.meses_hasta_cura = 2
                                THEN '2_MESES'
                            WHEN e.meses_hasta_cura = 3
                                THEN '3_MESES'
                            WHEN e.meses_hasta_cura BETWEEN 4 AND 6
                                THEN '4_6_MESES'
                            ELSE 'MAS_6_MESES'
                        END AS codigo,

                        CASE
                            WHEN e.fecha_cura IS NULL
                                THEN 'Sin curar'
                            WHEN e.meses_hasta_cura = 1
                                THEN '1 mes'
                            WHEN e.meses_hasta_cura = 2
                                THEN '2 meses'
                            WHEN e.meses_hasta_cura = 3
                                THEN '3 meses'
                            WHEN e.meses_hasta_cura BETWEEN 4 AND 6
                                THEN '4 a 6 meses'
                            ELSE 'Más de 6 meses'
                        END AS descripcion

                    FROM episodios_filtrados e
                )

                SELECT
                    d.orden,
                    d.codigo,
                    d.descripcion,
                    COUNT(*)::int AS episodios,

                    ROUND(
                        COUNT(*)::numeric
                        * 100
                        /
                        NULLIF(
                            (SELECT COUNT(*) FROM distribucion),
                            0
                        ),
                        4
                    ) AS porcentaje_episodios

                FROM distribucion d

                GROUP BY
                    d.orden,
                    d.codigo,
                    d.descripcion

                ORDER BY d.orden
                """;

        return jdbc.query(
                sql,
                parametros(
                        periodoDesde,
                        periodoHastaExclusivo,
                        idAgencia,
                        idLineaCredito,
                        edadEntrada
                ),
                new BeanPropertyRowMapper<>(
                        CuracionReincidenciaDistribucionCuraDTO.class
                )
        );
    }

    public List<CuracionReincidenciaPrimeraReincidenciaDTO> primeraReincidencia(
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {

        String sql = cteMotor() + """
                , meses AS (
                    SELECT generate_series(1, 6)::int AS mes
                ),

                base AS (
                    SELECT
                        e.meses_hasta_reincidencia
                    FROM episodios_filtrados e
                    WHERE e.seguimiento6m_completo
                )

                SELECT
                    m.mes,

                    COUNT(*) FILTER (
                        WHERE b.meses_hasta_reincidencia = m.mes
                    )::int AS reincidentes,

                    ROUND(
                        COUNT(*) FILTER (
                            WHERE b.meses_hasta_reincidencia = m.mes
                        )::numeric
                        * 100
                        /
                        NULLIF(
                            (SELECT COUNT(*) FROM base),
                            0
                        ),
                        4
                    ) AS porcentaje_sobre_curas_maduras

                FROM meses m

                LEFT JOIN base b
                       ON b.meses_hasta_reincidencia = m.mes

                GROUP BY m.mes

                ORDER BY m.mes
                """;

        return jdbc.query(
                sql,
                parametros(
                        periodoDesde,
                        periodoHastaExclusivo,
                        idAgencia,
                        idLineaCredito,
                        edadEntrada
                ),
                new BeanPropertyRowMapper<>(
                        CuracionReincidenciaPrimeraReincidenciaDTO.class
                )
        );
    }

    public List<CuracionReincidenciaDetalleDTO> detalle(
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada,
            String indicador
    ) {

        String sql = cteMotorDetalle() + """
                SELECT
                    e.id_cartera_credito,
                    e.numero_episodio,

                    e.id_agencia,
                    e.codigo_agencia,
                    e.nombre_agencia,

                    e.id_linea_credito,
                    e.codigo_linea_credito,
                    e.nombre_linea_credito,

                    e.pagare_cartera,
                    e.id_datos_personal,
                    e.tipo_documento,
                    e.documento,
                    e.nombre_completo,

                    e.periodo_inicio,
                    e.fecha_inicio,
                    e.edad_entrada,
                    e.maxima_edad_alcanzada,

                    e.saldo_inicio,
                    e.dias_mora_inicio,

                    (e.fecha_cura IS NOT NULL) AS curado,
                    e.fecha_cura,
                    e.meses_hasta_cura,
                    hc.saldo_credito_fecha_corte AS saldo_cura,

                    e.seguimiento6m_completo,

                    (
                        e.seguimiento6m_completo
                        AND e.fecha_reincidencia IS NOT NULL
                    ) AS reincidente6m,

                    e.fecha_reincidencia,
                    e.meses_hasta_reincidencia,

                    hr.edad_contable AS edad_reincidencia,
                    hr.saldo_credito_fecha_corte AS saldo_reincidencia,
                    hr.dias_mora AS dias_mora_reincidencia,

                    u.fecha_corte AS fecha_ultimo_corte_observado,
                    u.edad_contable AS edad_actual,
                    u.saldo_credito_fecha_corte AS saldo_actual,
                    u.dias_mora AS dias_mora_actual,

                    c.telefono,
                    c.celular,
                    c.correo

                FROM episodios_filtrados e

                LEFT JOIN historia hc
                       ON hc.id_cartera_credito = e.id_cartera_credito
                      AND hc.fecha_corte = e.fecha_cura

                LEFT JOIN historia hr
                       ON hr.id_cartera_credito = e.id_cartera_credito
                      AND hr.fecha_corte = e.fecha_reincidencia

                LEFT JOIN ultima_observacion u
                       ON u.id_cartera_credito = e.id_cartera_credito

                LEFT JOIN contactos c
                       ON c.id_datos_personal = e.id_datos_personal

                WHERE
                    CASE :indicador
                        WHEN 'TODOS'
                            THEN TRUE
                        WHEN 'CURADOS'
                            THEN e.fecha_cura IS NOT NULL
                        WHEN 'ABIERTOS'
                            THEN e.fecha_cura IS NULL
                        WHEN 'CURAS_MADURAS_6M'
                            THEN e.seguimiento6m_completo
                        WHEN 'REINCIDENTES_6M'
                            THEN e.seguimiento6m_completo
                             AND e.fecha_reincidencia IS NOT NULL
                        WHEN 'NO_REINCIDENTES_6M'
                            THEN e.seguimiento6m_completo
                             AND e.fecha_reincidencia IS NULL
                        ELSE FALSE
                    END

                ORDER BY
                    e.fecha_inicio DESC,
                    e.id_cartera_credito,
                    e.numero_episodio
                """;

        MapSqlParameterSource params =
                parametros(
                        periodoDesde,
                        periodoHastaExclusivo,
                        idAgencia,
                        idLineaCredito,
                        edadEntrada
                )
                .addValue("indicador", indicador);

        return jdbc.query(
                sql,
                params,
                new BeanPropertyRowMapper<>(
                        CuracionReincidenciaDetalleDTO.class
                )
        );
    }

    private String cteMotor() {
        return """
                WITH historia AS MATERIALIZED (
                    SELECT DISTINCT ON (
                        h.id_cartera_credito,
                        h.fecha_corte
                    )
                        h.id_cartera_credito,
                        h.fecha_corte::date AS fecha_corte,
                        h.edad_contable_resultado AS edad_contable,

                        h.id_agencia,

                        h.id_linea_credito,
                        h.codigo_linea_credito,
                        h.nombre_linea_credito,

                        h.pagare_cartera,
                        h.id_datos_personal,
                        h.tipo_documento,
                        h.documento,
                        h.nombre_completo,

                        h.saldo_credito_fecha_corte,
                        h.dias_mora

                    FROM cartera.vw_cartera_resultados_mensuales_total h

                    WHERE h.edad_contable_resultado
                          IN ('A','B','C','D','E')

                    ORDER BY
                        h.id_cartera_credito,
                        h.fecha_corte,
                        h.id_cierre_cartera DESC
                ),

                ultimo_corte AS MATERIALIZED (
                    SELECT MAX(fecha_corte) AS fecha
                    FROM historia
                ),

                secuencia AS MATERIALIZED (
                    SELECT
                        h.*,

                        LAG(h.edad_contable) OVER (
                            PARTITION BY h.id_cartera_credito
                            ORDER BY h.fecha_corte
                        ) AS edad_anterior

                    FROM historia h
                ),

                marcas AS MATERIALIZED (
                    SELECT
                        s.*,

                        CASE
                            WHEN s.edad_contable IN ('B','C','D','E')
                             AND COALESCE(s.edad_anterior, 'A') = 'A'
                            THEN 1
                            ELSE 0
                        END AS inicia_episodio,

                        CASE
                            WHEN s.edad_contable = 'A'
                             AND s.edad_anterior IN ('B','C','D','E')
                            THEN 1
                            ELSE 0
                        END AS es_cura

                    FROM secuencia s
                ),

                numerada AS MATERIALIZED (
                    SELECT
                        m.*,

                        SUM(m.inicia_episodio) OVER (
                            PARTITION BY m.id_cartera_credito
                            ORDER BY m.fecha_corte
                            ROWS BETWEEN UNBOUNDED PRECEDING
                                     AND CURRENT ROW
                        ) AS numero_episodio

                    FROM marcas m
                ),

                episodios_base AS MATERIALIZED (
                    SELECT
                        n.id_cartera_credito,
                        n.numero_episodio,

                        MIN(n.fecha_corte)
                            FILTER (
                                WHERE n.inicia_episodio = 1
                            ) AS fecha_inicio,

                        MIN(n.fecha_corte)
                            FILTER (
                                WHERE n.es_cura = 1
                            ) AS fecha_cura,

                        MAX(
                            CASE n.edad_contable
                                WHEN 'B' THEN 2
                                WHEN 'C' THEN 3
                                WHEN 'D' THEN 4
                                WHEN 'E' THEN 5
                                ELSE 1
                            END
                        ) FILTER (
                            WHERE n.edad_contable IN ('B','C','D','E')
                        ) AS maxima_edad_rank

                    FROM numerada n

                    WHERE n.numero_episodio > 0

                    GROUP BY
                        n.id_cartera_credito,
                        n.numero_episodio
                ),

                episodios AS MATERIALIZED (
                    SELECT
                        e.id_cartera_credito,
                        e.numero_episodio,
                        e.fecha_inicio,
                        DATE_TRUNC(
                            'month',
                            e.fecha_inicio
                        )::date AS periodo_inicio,

                        e.fecha_cura,

                        i.id_agencia,
                        a.codigo_agencia,
                        a.nombre_agencia,

                        i.id_linea_credito,
                        i.codigo_linea_credito,
                        i.nombre_linea_credito,

                        i.pagare_cartera,
                        i.id_datos_personal,
                        i.tipo_documento,
                        i.documento,
                        i.nombre_completo,

                        i.edad_contable AS edad_entrada,

                        CASE e.maxima_edad_rank
                            WHEN 2 THEN 'B'
                            WHEN 3 THEN 'C'
                            WHEN 4 THEN 'D'
                            WHEN 5 THEN 'E'
                        END AS maxima_edad_alcanzada,

                        i.saldo_credito_fecha_corte AS saldo_inicio,
                        i.dias_mora AS dias_mora_inicio,

                        CASE
                            WHEN e.fecha_cura IS NOT NULL
                            THEN
                                (
                                    EXTRACT(
                                        YEAR FROM e.fecha_cura
                                    )::int * 12
                                    +
                                    EXTRACT(
                                        MONTH FROM e.fecha_cura
                                    )::int
                                )
                                -
                                (
                                    EXTRACT(
                                        YEAR FROM e.fecha_inicio
                                    )::int * 12
                                    +
                                    EXTRACT(
                                        MONTH FROM e.fecha_inicio
                                    )::int
                                )
                        END AS meses_hasta_cura

                    FROM episodios_base e

                    JOIN historia i
                      ON i.id_cartera_credito =
                         e.id_cartera_credito
                     AND i.fecha_corte =
                         e.fecha_inicio

                    LEFT JOIN general.datos_agencias a
                           ON a.id_agencia = i.id_agencia

                    WHERE e.fecha_inicio IS NOT NULL
                ),

                reincidencias AS MATERIALIZED (
                    SELECT
                        e.*,
                        MIN(r.fecha_corte) AS fecha_reincidencia

                    FROM episodios e

                    LEFT JOIN historia r
                      ON r.id_cartera_credito =
                         e.id_cartera_credito
                     AND e.fecha_cura IS NOT NULL
                     AND r.fecha_corte >
                         e.fecha_cura
                     AND r.fecha_corte <=
                         e.fecha_cura
                         + INTERVAL '6 months'
                     AND r.edad_contable
                         IN ('B','C','D','E')

                    GROUP BY
                        e.id_cartera_credito,
                        e.numero_episodio,
                        e.fecha_inicio,
                        e.periodo_inicio,
                        e.fecha_cura,

                        e.id_agencia,
                        e.codigo_agencia,
                        e.nombre_agencia,

                        e.id_linea_credito,
                        e.codigo_linea_credito,
                        e.nombre_linea_credito,

                        e.pagare_cartera,
                        e.id_datos_personal,
                        e.tipo_documento,
                        e.documento,
                        e.nombre_completo,

                        e.edad_entrada,
                        e.maxima_edad_alcanzada,

                        e.saldo_inicio,
                        e.dias_mora_inicio,

                        e.meses_hasta_cura
                ),

                resultado AS MATERIALIZED (
                    SELECT
                        r.*,

                        (
                            r.fecha_cura IS NOT NULL
                            AND r.fecha_cura <=
                                (
                                    SELECT fecha
                                    FROM ultimo_corte
                                )
                                - INTERVAL '6 months'
                        ) AS seguimiento6m_completo,

                        CASE
                            WHEN r.fecha_reincidencia IS NOT NULL
                            THEN
                                (
                                    EXTRACT(
                                        YEAR FROM r.fecha_reincidencia
                                    )::int * 12
                                    +
                                    EXTRACT(
                                        MONTH FROM r.fecha_reincidencia
                                    )::int
                                )
                                -
                                (
                                    EXTRACT(
                                        YEAR FROM r.fecha_cura
                                    )::int * 12
                                    +
                                    EXTRACT(
                                        MONTH FROM r.fecha_cura
                                    )::int
                                )
                        END AS meses_hasta_reincidencia

                    FROM reincidencias r
                ),

                episodios_filtrados AS MATERIALIZED (
                    SELECT *
                    FROM resultado e
                    WHERE e.fecha_inicio >= :periodoDesde
                      AND e.fecha_inicio < :periodoHastaExclusivo

                      AND (
                          :idAgencia IS NULL
                          OR e.id_agencia = :idAgencia
                      )

                      AND (
                          :idLineaCredito IS NULL
                          OR e.id_linea_credito = :idLineaCredito
                      )

                      AND (
                          :edadEntrada IS NULL
                          OR e.edad_entrada = :edadEntrada
                      )
                )
                """;
    }

    private String cteMotorDetalle() {
        return cteMotor() + """
                , ultima_observacion AS MATERIALIZED (
                    SELECT DISTINCT ON (
                        h.id_cartera_credito
                    )
                        h.id_cartera_credito,
                        h.fecha_corte,
                        h.edad_contable,
                        h.saldo_credito_fecha_corte,
                        h.dias_mora

                    FROM historia h

                    ORDER BY
                        h.id_cartera_credito,
                        h.fecha_corte DESC
                ),

                contactos AS MATERIALIZED (
                    SELECT
                        dp.id_datos_personal,
                        MAX(dp.telefono) AS telefono,
                        MAX(dp.celular_uno) AS celular,
                        MAX(dp.correo_personal) AS correo

                    FROM reporting.vw_datos_personales_operativa dp

                    GROUP BY dp.id_datos_personal
                )
                """;
    }

    private MapSqlParameterSource parametros(
            LocalDate periodoDesde,
            LocalDate periodoHastaExclusivo,
            Integer idAgencia,
            Integer idLineaCredito,
            String edadEntrada
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        "periodoDesde",
                        periodoDesde,
                        Types.DATE
                )
                .addValue(
                        "periodoHastaExclusivo",
                        periodoHastaExclusivo,
                        Types.DATE
                )
                .addValue(
                        "idAgencia",
                        idAgencia,
                        Types.INTEGER
                )
                .addValue(
                        "idLineaCredito",
                        idLineaCredito,
                        Types.INTEGER
                )
                .addValue(
                        "edadEntrada",
                        edadEntrada,
                        Types.VARCHAR
                );
    }
}

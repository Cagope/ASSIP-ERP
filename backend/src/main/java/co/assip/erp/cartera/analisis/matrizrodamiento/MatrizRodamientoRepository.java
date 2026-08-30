package co.assip.erp.cartera.analisis.matrizrodamiento;

import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoCeldaDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoCorteDTO;
import co.assip.erp.cartera.analisis.matrizrodamiento.dto.MatrizRodamientoDetalleDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class MatrizRodamientoRepository {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<MatrizRodamientoDetalleDTO>
            DETALLE_MAPPER =
            crearMapper(
                    MatrizRodamientoDetalleDTO.class
            );


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MatrizRodamientoRepository(
            NamedParameterJdbcTemplate jdbc
    ) {

        this.jdbc = jdbc;
    }


    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    public List<MatrizRodamientoCorteDTO>
    listarCortesDisponibles() {

        String sql = """
                SELECT
                    h.fecha_corte,

                    COUNT(
                        DISTINCT h.id_cartera_credito
                    )::integer
                        AS cantidad_creditos

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE COALESCE(
                          h.saldo_credito_fecha_corte,
                          0
                      ) > 0

                GROUP BY
                    h.fecha_corte

                ORDER BY
                    h.fecha_corte DESC
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (rs, rowNum) -> {

                    MatrizRodamientoCorteDTO dto =
                            new MatrizRodamientoCorteDTO();

                    dto.setFechaCorte(
                            rs.getObject(
                                    "fecha_corte",
                                    LocalDate.class
                            )
                    );

                    dto.setCantidadCreditos(
                            rs.getInt(
                                    "cantidad_creditos"
                            )
                    );

                    return dto;
                }
        );
    }


    // =========================================================
    // EXISTENCIA DEL CORTE
    // =========================================================

    public boolean existeCorte(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT EXISTS (

                    SELECT 1

                    FROM cartera.cierres_cartera c

                    WHERE c.fecha_corte =
                          :fechaCorte
                )
                """;

        Boolean existe =
                jdbc.queryForObject(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "fechaCorte",
                                        fechaCorte
                                ),
                        Boolean.class
                );

        return Boolean.TRUE.equals(
                existe
        );
    }


    // =========================================================
    // POBLACIÓN DE PARTIDA
    // =========================================================

    public int contarPoblacionPartida(
            String tipoPartida,
            LocalDate fechaPartida
    ) {

        if ("ACTUAL".equals(tipoPartida)) {

            return contarPoblacionActual();
        }

        return contarPoblacionCorte(
                fechaPartida
        );
    }


    // =========================================================
    // POBLACIÓN ACTUAL
    // =========================================================

    private int contarPoblacionActual() {

        String sql = """
                SELECT
                    COUNT(
                        DISTINCT d.id_cartera_credito
                    )::integer

                FROM cartera.vw_cartera_vector_comportamiento_detalle d

                WHERE d.tipo_posicion = 'ACTUAL'

                  AND COALESCE(
                          d.saldo_actual_maestro,
                          0
                      ) > 0

                  AND COALESCE(
                          d.codigo_estado_cartera_actual,
                          ''
                      ) = 'A'
                """;

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        new MapSqlParameterSource(),
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


    // =========================================================
    // POBLACIÓN CORTE
    // =========================================================

    private int contarPoblacionCorte(
            LocalDate fechaPartida
    ) {

        String sql = """
                WITH poblacion AS (

                    SELECT DISTINCT ON (
                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera
                    )

                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera

                    FROM cartera.vw_cartera_resultados_mensuales_total h

                    WHERE h.fecha_corte =
                          :fechaPartida

                      AND COALESCE(
                              h.saldo_credito_fecha_corte,
                              0
                          ) > 0

                      AND COALESCE(
                              h.codigo_estado_cartera,
                              ''
                          ) = 'A'

                    ORDER BY
                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera,
                        h.id_cierre_cartera DESC,
                        h.id_cierre_cartera_credito DESC
                )

                SELECT
                    COUNT(*)::integer

                FROM poblacion
                """;

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "fechaPartida",
                                        fechaPartida
                                ),
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


    // =========================================================
    // MATRIZ AGREGADA
    // =========================================================

    /**
     * Devuelve SIEMPRE las 25 combinaciones A-E.
     *
     * Los movimientos inexistentes regresan:
     *
     * cantidad = 0
     * valor    = 0
     */
    public List<MatrizRodamientoCeldaDTO>
    listarCeldasAgregadas(
            String tipoPartida,
            LocalDate fechaPartida,
            LocalDate fechaComparacion
    ) {

        String sql =
                construirSqlMatrizAgregada(
                        tipoPartida
                );

        return jdbc.query(
                sql,
                parametros(
                        fechaPartida,
                        fechaComparacion
                ),
                (rs, rowNum) -> {

                    MatrizRodamientoCeldaDTO dto =
                            new MatrizRodamientoCeldaDTO();

                    dto.setCategoriaAnterior(
                            rs.getString(
                                    "categoria_anterior"
                            )
                    );

                    dto.setCategoriaPartida(
                            rs.getString(
                                    "categoria_partida"
                            )
                    );

                    dto.setCantidad(
                            rs.getInt(
                                    "cantidad"
                            )
                    );

                    BigDecimal valor =
                            rs.getBigDecimal(
                                    "valor"
                            );

                    dto.setValor(
                            valor != null
                                    ? valor
                                    : BigDecimal.ZERO
                    );

                    dto.setPorcentajeCantidad(
                            BigDecimal.ZERO
                    );

                    dto.setPorcentajeValor(
                            BigDecimal.ZERO
                    );

                    return dto;
                }
        );
    }


    // =========================================================
    // DETALLE INDIVIDUAL DE CELDA
    // =========================================================

    public List<MatrizRodamientoDetalleDTO>
    listarDetalleCelda(
            String tipoPartida,
            LocalDate fechaPartida,
            LocalDate fechaComparacion,
            String categoriaAnterior,
            String categoriaPartida
    ) {

        String sql =
                construirSqlDetalle(
                        tipoPartida
                );

        MapSqlParameterSource params =
                parametros(
                        fechaPartida,
                        fechaComparacion
                )
                        .addValue(
                                "categoriaAnterior",
                                categoriaAnterior
                        )
                        .addValue(
                                "categoriaPartida",
                                categoriaPartida
                        );

        return jdbc.query(
                sql,
                params,
                DETALLE_MAPPER
        );
    }


    // =========================================================
    // SQL MATRIZ AGREGADA - CORTE
    // =========================================================

    private String sqlMatrizCorteContraCorte() {

        return """
                WITH categorias AS (

                    SELECT *
                    FROM (
                        VALUES
                            ('A'),
                            ('B'),
                            ('C'),
                            ('D'),
                            ('E')
                    ) AS x(categoria)
                ),

                partida AS (

                    SELECT DISTINCT ON (
                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera
                    )

                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera,

                        COALESCE(
                            h.dias_mora,
                            0
                        )::integer
                            AS dias_mora_partida,

                        COALESCE(
                            h.saldo_credito_fecha_corte,
                            0
                        )
                            AS saldo_partida

                    FROM cartera.vw_cartera_resultados_mensuales_total h

                    WHERE h.fecha_corte =
                          :fechaPartida

                      AND COALESCE(
                              h.saldo_credito_fecha_corte,
                              0
                          ) > 0

                      AND COALESCE(
                              h.codigo_estado_cartera,
                              ''
                          ) = 'A'

                    ORDER BY
                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera,
                        h.id_cierre_cartera DESC,
                        h.id_cierre_cartera_credito DESC
                ),

                comparacion AS (

                    SELECT DISTINCT ON (
                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera
                    )

                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera,

                        COALESCE(
                            h.dias_mora,
                            0
                        )::integer
                            AS dias_mora_anterior

                    FROM cartera.vw_cartera_resultados_mensuales_total h

                    WHERE h.fecha_corte =
                          :fechaComparacion

                    ORDER BY
                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera,
                        h.id_cierre_cartera DESC,
                        h.id_cierre_cartera_credito DESC
                ),

                movimientos AS (

                    SELECT

                        CASE
                            WHEN c.dias_mora_anterior <= 30
                                THEN 'A'

                            WHEN c.dias_mora_anterior < 61
                                THEN 'B'

                            WHEN c.dias_mora_anterior < 91
                                THEN 'C'

                            WHEN c.dias_mora_anterior < 181
                                THEN 'D'

                            ELSE 'E'
                        END
                            AS categoria_anterior,

                        CASE
                            WHEN p.dias_mora_partida <= 30
                                THEN 'A'

                            WHEN p.dias_mora_partida < 61
                                THEN 'B'

                            WHEN p.dias_mora_partida < 91
                                THEN 'C'

                            WHEN p.dias_mora_partida < 181
                                THEN 'D'

                            ELSE 'E'
                        END
                            AS categoria_partida,

                        p.saldo_partida

                    FROM partida p

                    INNER JOIN comparacion c
                            ON c.id_agencia =
                               p.id_agencia

                           AND c.id_linea_credito =
                               p.id_linea_credito

                           AND c.pagare_cartera =
                               p.pagare_cartera
                ),

                agregado AS (

                    SELECT
                        categoria_anterior,
                        categoria_partida,

                        COUNT(*)::integer
                            AS cantidad,

                        COALESCE(
                            SUM(saldo_partida),
                            0
                        )
                            AS valor

                    FROM movimientos

                    GROUP BY
                        categoria_anterior,
                        categoria_partida
                )

                SELECT
                    ca.categoria
                        AS categoria_anterior,

                    cp.categoria
                        AS categoria_partida,

                    COALESCE(
                        a.cantidad,
                        0
                    )::integer
                        AS cantidad,

                    COALESCE(
                        a.valor,
                        0
                    )::numeric
                        AS valor

                FROM categorias ca

                CROSS JOIN categorias cp

                LEFT JOIN agregado a
                       ON a.categoria_anterior =
                          ca.categoria

                      AND a.categoria_partida =
                          cp.categoria

                ORDER BY
                    ca.categoria,
                    cp.categoria
                """;
    }


    // =========================================================
    // SQL MATRIZ AGREGADA - ACTUAL
    // =========================================================

    private String sqlMatrizActualContraCorte() {

        return """
                WITH categorias AS (

                    SELECT *
                    FROM (
                        VALUES
                            ('A'),
                            ('B'),
                            ('C'),
                            ('D'),
                            ('E')
                    ) AS x(categoria)
                ),

                partida AS (

                    SELECT DISTINCT ON (
                        d.id_agencia,
                        d.id_linea_credito,
                        d.pagare_cartera
                    )

                        d.id_agencia,
                        d.id_linea_credito,
                        d.pagare_cartera,

                        COALESCE(
                            d.dias_mora,
                            0
                        )::integer
                            AS dias_mora_partida,

                        COALESCE(
                            d.saldo_actual_maestro,
                            0
                        )
                            AS saldo_partida

                    FROM cartera.vw_cartera_vector_comportamiento_detalle d

                    WHERE d.tipo_posicion =
                          'ACTUAL'

                      AND COALESCE(
                              d.saldo_actual_maestro,
                              0
                          ) > 0

                      AND COALESCE(
                              d.codigo_estado_cartera_actual,
                              ''
                          ) = 'A'

                    ORDER BY
                        d.id_agencia,
                        d.id_linea_credito,
                        d.pagare_cartera,
                        d.id_cartera_credito
                ),

                comparacion AS (

                    SELECT DISTINCT ON (
                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera
                    )

                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera,

                        COALESCE(
                            h.dias_mora,
                            0
                        )::integer
                            AS dias_mora_anterior

                    FROM cartera.vw_cartera_resultados_mensuales_total h

                    WHERE h.fecha_corte =
                          :fechaComparacion

                    ORDER BY
                        h.id_agencia,
                        h.id_linea_credito,
                        h.pagare_cartera,
                        h.id_cierre_cartera DESC,
                        h.id_cierre_cartera_credito DESC
                ),

                movimientos AS (

                    SELECT

                        CASE
                            WHEN c.dias_mora_anterior <= 30
                                THEN 'A'

                            WHEN c.dias_mora_anterior < 61
                                THEN 'B'

                            WHEN c.dias_mora_anterior < 91
                                THEN 'C'

                            WHEN c.dias_mora_anterior < 181
                                THEN 'D'

                            ELSE 'E'
                        END
                            AS categoria_anterior,

                        CASE
                            WHEN p.dias_mora_partida <= 30
                                THEN 'A'

                            WHEN p.dias_mora_partida < 61
                                THEN 'B'

                            WHEN p.dias_mora_partida < 91
                                THEN 'C'

                            WHEN p.dias_mora_partida < 181
                                THEN 'D'

                            ELSE 'E'
                        END
                            AS categoria_partida,

                        p.saldo_partida

                    FROM partida p

                    INNER JOIN comparacion c
                            ON c.id_agencia =
                               p.id_agencia

                           AND c.id_linea_credito =
                               p.id_linea_credito

                           AND c.pagare_cartera =
                               p.pagare_cartera
                ),

                agregado AS (

                    SELECT
                        categoria_anterior,
                        categoria_partida,

                        COUNT(*)::integer
                            AS cantidad,

                        COALESCE(
                            SUM(saldo_partida),
                            0
                        )
                            AS valor

                    FROM movimientos

                    GROUP BY
                        categoria_anterior,
                        categoria_partida
                )

                SELECT
                    ca.categoria
                        AS categoria_anterior,

                    cp.categoria
                        AS categoria_partida,

                    COALESCE(
                        a.cantidad,
                        0
                    )::integer
                        AS cantidad,

                    COALESCE(
                        a.valor,
                        0
                    )::numeric
                        AS valor

                FROM categorias ca

                CROSS JOIN categorias cp

                LEFT JOIN agregado a
                       ON a.categoria_anterior =
                          ca.categoria

                      AND a.categoria_partida =
                          cp.categoria

                ORDER BY
                    ca.categoria,
                    cp.categoria
                """;
    }


    // =========================================================
// DETALLE - CORTE CONTRA CORTE
// =========================================================

    private String sqlDetalleCorteContraCorte() {

        return """
            WITH partida AS MATERIALIZED (

                SELECT DISTINCT ON (
                    h.id_agencia,
                    h.id_linea_credito,
                    h.pagare_cartera
                )

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

                    h.fecha_desembolso,

                    COALESCE(
                        h.dias_mora,
                        0
                    )::integer
                        AS dias_mora_partida,

                    COALESCE(
                        h.saldo_credito_fecha_corte,
                        0
                    )
                        AS saldo_partida

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE h.fecha_corte =
                      :fechaPartida

                  AND COALESCE(
                          h.saldo_credito_fecha_corte,
                          0
                      ) > 0

                  AND COALESCE(
                          h.codigo_estado_cartera,
                          ''
                      ) = 'A'

                ORDER BY
                    h.id_agencia,
                    h.id_linea_credito,
                    h.pagare_cartera,
                    h.id_cierre_cartera DESC,
                    h.id_cierre_cartera_credito DESC
            ),

            comparacion AS MATERIALIZED (

                SELECT DISTINCT ON (
                    h.id_agencia,
                    h.id_linea_credito,
                    h.pagare_cartera
                )

                    h.id_agencia,
                    h.id_linea_credito,
                    h.pagare_cartera,

                    COALESCE(
                        h.dias_mora,
                        0
                    )::integer
                        AS dias_mora_anterior,

                    COALESCE(
                        h.saldo_credito_fecha_corte,
                        0
                    )
                        AS saldo_anterior

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE h.fecha_corte =
                      :fechaComparacion

                ORDER BY
                    h.id_agencia,
                    h.id_linea_credito,
                    h.pagare_cartera,
                    h.id_cierre_cartera DESC,
                    h.id_cierre_cartera_credito DESC
            ),

            movimientos AS MATERIALIZED (

                SELECT
                    p.*,

                    c.dias_mora_anterior,
                    c.saldo_anterior,

                    CASE
                        WHEN c.dias_mora_anterior <= 30
                            THEN 'A'
                        WHEN c.dias_mora_anterior < 61
                            THEN 'B'
                        WHEN c.dias_mora_anterior < 91
                            THEN 'C'
                        WHEN c.dias_mora_anterior < 181
                            THEN 'D'
                        ELSE 'E'
                    END
                        AS categoria_anterior,

                    CASE
                        WHEN p.dias_mora_partida <= 30
                            THEN 'A'
                        WHEN p.dias_mora_partida < 61
                            THEN 'B'
                        WHEN p.dias_mora_partida < 91
                            THEN 'C'
                        WHEN p.dias_mora_partida < 181
                            THEN 'D'
                        ELSE 'E'
                    END
                        AS categoria_partida

                FROM partida p

                INNER JOIN comparacion c
                        ON c.id_agencia =
                           p.id_agencia

                       AND c.id_linea_credito =
                           p.id_linea_credito

                       AND c.pagare_cartera =
                           p.pagare_cartera
            ),

            celda AS MATERIALIZED (

                SELECT
                    m.*

                FROM movimientos m

                WHERE m.categoria_anterior =
                      :categoriaAnterior

                  AND m.categoria_partida =
                      :categoriaPartida
            )

            SELECT
                m.id_cartera_credito,
                m.id_agencia,
                m.id_linea_credito,
                m.codigo_linea_credito,
                m.nombre_linea_credito,
                m.pagare_cartera,

                m.id_datos_personal,
                m.tipo_documento,
                m.documento,
                m.nombre_completo,

                dp.telefono,

                dp.celular_uno
                    AS celular,

                dp.correo_personal
                    AS correo,

                m.fecha_desembolso,

                CAST(
                    :fechaComparacion
                    AS date
                )
                    AS fecha_comparacion,

                m.dias_mora_anterior,
                m.categoria_anterior,

                COALESCE(
                    m.saldo_anterior,
                    0
                )
                    AS saldo_anterior,

                'CORTE'
                    AS tipo_partida,

                CAST(
                    :fechaPartida
                    AS date
                )
                    AS fecha_partida,

                m.dias_mora_partida,
                m.categoria_partida,

                COALESCE(
                    m.saldo_partida,
                    0
                )
                    AS saldo_partida

            FROM celda m

            LEFT JOIN reporting.vw_datos_personales_operativa dp
                   ON dp.id_datos_personal =
                      m.id_datos_personal

            ORDER BY
                m.id_agencia,
                m.documento,
                m.pagare_cartera,
                m.id_cartera_credito
            """;
    }


    // =========================================================
// DETALLE - ACTUAL CONTRA CORTE
// =========================================================

    private String sqlDetalleActualContraCorte() {

        return """
            WITH partida AS MATERIALIZED (

                SELECT DISTINCT ON (
                    d.id_agencia,
                    d.id_linea_credito,
                    d.pagare_cartera
                )

                    d.id_cartera_credito,
                    d.id_agencia,
                    d.id_linea_credito,
                    d.codigo_linea_credito,
                    d.nombre_linea_credito,
                    d.pagare_cartera,

                    d.id_datos_personal,
                    d.tipo_documento,
                    d.documento,
                    d.nombre_completo,

                    cc.fecha_desembolso,

                    COALESCE(
                        d.dias_mora,
                        0
                    )::integer
                        AS dias_mora_partida,

                    COALESCE(
                        d.saldo_actual_maestro,
                        0
                    )
                        AS saldo_partida

                FROM cartera.vw_cartera_vector_comportamiento_detalle d

                INNER JOIN cartera.carteras_creditos cc
                        ON cc.id_cartera_credito =
                           d.id_cartera_credito

                WHERE d.tipo_posicion =
                      'ACTUAL'

                  AND COALESCE(
                          d.saldo_actual_maestro,
                          0
                      ) > 0

                  AND COALESCE(
                          d.codigo_estado_cartera_actual,
                          ''
                      ) = 'A'

                ORDER BY
                    d.id_agencia,
                    d.id_linea_credito,
                    d.pagare_cartera,
                    d.id_cartera_credito
            ),

            comparacion AS MATERIALIZED (

                SELECT DISTINCT ON (
                    h.id_agencia,
                    h.id_linea_credito,
                    h.pagare_cartera
                )

                    h.id_agencia,
                    h.id_linea_credito,
                    h.pagare_cartera,

                    COALESCE(
                        h.dias_mora,
                        0
                    )::integer
                        AS dias_mora_anterior,

                    COALESCE(
                        h.saldo_credito_fecha_corte,
                        0
                    )
                        AS saldo_anterior

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE h.fecha_corte =
                      :fechaComparacion

                ORDER BY
                    h.id_agencia,
                    h.id_linea_credito,
                    h.pagare_cartera,
                    h.id_cierre_cartera DESC,
                    h.id_cierre_cartera_credito DESC
            ),

            movimientos AS MATERIALIZED (

                SELECT
                    p.*,

                    c.dias_mora_anterior,
                    c.saldo_anterior,

                    CASE
                        WHEN c.dias_mora_anterior <= 30
                            THEN 'A'
                        WHEN c.dias_mora_anterior < 61
                            THEN 'B'
                        WHEN c.dias_mora_anterior < 91
                            THEN 'C'
                        WHEN c.dias_mora_anterior < 181
                            THEN 'D'
                        ELSE 'E'
                    END
                        AS categoria_anterior,

                    CASE
                        WHEN p.dias_mora_partida <= 30
                            THEN 'A'
                        WHEN p.dias_mora_partida < 61
                            THEN 'B'
                        WHEN p.dias_mora_partida < 91
                            THEN 'C'
                        WHEN p.dias_mora_partida < 181
                            THEN 'D'
                        ELSE 'E'
                    END
                        AS categoria_partida

                FROM partida p

                INNER JOIN comparacion c
                        ON c.id_agencia =
                           p.id_agencia

                       AND c.id_linea_credito =
                           p.id_linea_credito

                       AND c.pagare_cartera =
                           p.pagare_cartera
            ),

            celda AS MATERIALIZED (

                SELECT
                    m.*

                FROM movimientos m

                WHERE m.categoria_anterior =
                      :categoriaAnterior

                  AND m.categoria_partida =
                      :categoriaPartida
            )

            SELECT
                m.id_cartera_credito,
                m.id_agencia,
                m.id_linea_credito,
                m.codigo_linea_credito,
                m.nombre_linea_credito,
                m.pagare_cartera,

                m.id_datos_personal,
                m.tipo_documento,
                m.documento,
                m.nombre_completo,

                dp.telefono,

                dp.celular_uno
                    AS celular,

                dp.correo_personal
                    AS correo,

                m.fecha_desembolso,

                CAST(
                    :fechaComparacion
                    AS date
                )
                    AS fecha_comparacion,

                m.dias_mora_anterior,
                m.categoria_anterior,

                COALESCE(
                    m.saldo_anterior,
                    0
                )
                    AS saldo_anterior,

                'ACTUAL'
                    AS tipo_partida,

                CURRENT_DATE
                    AS fecha_partida,

                m.dias_mora_partida,
                m.categoria_partida,

                COALESCE(
                    m.saldo_partida,
                    0
                )
                    AS saldo_partida

            FROM celda m

            LEFT JOIN reporting.vw_datos_personales_operativa dp
                   ON dp.id_datos_personal =
                      m.id_datos_personal

            ORDER BY
                m.id_agencia,
                m.documento,
                m.pagare_cartera,
                m.id_cartera_credito
            """;
    }


    // =========================================================
    // SELECCIÓN SQL MATRIZ
    // =========================================================

    private String construirSqlMatrizAgregada(
            String tipoPartida
    ) {

        if ("ACTUAL".equals(tipoPartida)) {

            return sqlMatrizActualContraCorte();
        }

        return sqlMatrizCorteContraCorte();
    }


    // =========================================================
    // SELECCIÓN SQL DETALLE
    // =========================================================

    private String construirSqlDetalle(
            String tipoPartida
    ) {

        if ("ACTUAL".equals(tipoPartida)) {

            return sqlDetalleActualContraCorte();
        }

        return sqlDetalleCorteContraCorte();
    }


    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource parametros(
            LocalDate fechaPartida,
            LocalDate fechaComparacion
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "fechaPartida",
                        fechaPartida
                )
                .addValue(
                        "fechaComparacion",
                        fechaComparacion
                );
    }


    // =========================================================
    // MAPPER GENÉRICO
    // =========================================================

    private static <T>
    BeanPropertyRowMapper<T> crearMapper(
            Class<T> tipo
    ) {

        BeanPropertyRowMapper<T> mapper =
                new BeanPropertyRowMapper<>(
                        tipo
                );

        mapper.setPrimitivesDefaultedForNullValue(
                true
        );

        return mapper;
    }
}
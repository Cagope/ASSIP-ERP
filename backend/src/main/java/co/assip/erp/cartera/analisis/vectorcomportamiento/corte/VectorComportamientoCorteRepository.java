package co.assip.erp.cartera.analisis.vectorcomportamiento.corte;

import co.assip.erp.cartera.analisis.vectorcomportamiento.corte.dto.VectorComportamientoCorteDetalleDTO;
import co.assip.erp.cartera.analisis.vectorcomportamiento.corte.dto.VectorComportamientoCorteResumenDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


/**
 * Repositorio del proceso de análisis
 * Vector de Comportamiento por Corte.
 *
 * Lógica del Vector:
 *
 * 1. El usuario selecciona una fecha de corte.
 *
 * 2. Ese corte se convierte en la posición de referencia
 *    del Vector y se trata conceptualmente como el ACTUAL.
 *
 * 3. La población corresponde exclusivamente a los créditos
 *    cuyo saldo en la fecha de corte seleccionada es mayor que cero.
 *
 * 4. No se utiliza el saldo actual del maestro de cartera
 *    para determinar la población.
 *
 * 5. Por cada crédito:
 *
 *      Posición 1:
 *          corte seleccionado.
 *
 *      Posiciones 2 a 13:
 *          máximo 12 cierres anteriores al corte seleccionado,
 *          ordenados del más reciente al más antiguo.
 *
 * 6. El resumen reproduce la lógica del Vector Actual:
 *
 *      - los datos principales corresponden al corte seleccionado;
 *      - los indicadores históricos se calculan exclusivamente
 *        sobre los 12 cierres anteriores;
 *      - el corte seleccionado no forma parte de las métricas
 *        históricas.
 *
 * 7. Los datos financieros, de comportamiento y riesgo
 *    provienen exclusivamente de los cierres históricos.
 *
 * 8. Los datos de contacto son únicamente informativos
 *    y nunca condicionan la población.
 *
 * Repositorio exclusivamente de lectura.
 */
@Repository
public class VectorComportamientoCorteRepository {

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // MAPPERS
    // =========================================================

    private static final BeanPropertyRowMapper<VectorComportamientoCorteResumenDTO>
            RESUMEN_MAPPER =
            crearMapper(
                    VectorComportamientoCorteResumenDTO.class
            );

    private static final BeanPropertyRowMapper<VectorComportamientoCorteDetalleDTO>
            DETALLE_MAPPER =
            crearMapper(
                    VectorComportamientoCorteDetalleDTO.class
            );


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public VectorComportamientoCorteRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    /**
     * Lista las fechas de corte disponibles.
     *
     * Solo devuelve fechas que contienen por lo menos
     * un crédito con saldo mayor que cero.
     */
    public List<LocalDate> listarFechasCorteDisponibles() {

        String sql = """
                SELECT DISTINCT
                    h.fecha_corte

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE COALESCE(
                          h.saldo_credito_fecha_corte,
                          0
                      ) > 0

                ORDER BY
                    h.fecha_corte DESC
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


    // =========================================================
    // RESUMEN - CARTERA DEL CORTE
    // =========================================================

    /**
     * Construye una fila resumen por cada crédito
     * perteneciente a la población del corte seleccionado.
     *
     * El corte seleccionado se trata como la posición actual
     * del Vector.
     *
     * Las métricas históricas consideran máximo
     * los 12 cierres inmediatamente anteriores.
     */
    public List<VectorComportamientoCorteResumenDTO>
    listarResumenPorCorte(
            LocalDate fechaCorte
    ) {

        String sql = SQL_RESUMEN_BASE + """

                ORDER BY
                    u.id_agencia,
                    u.documento,
                    u.pagare_cartera,
                    u.id_cartera_credito
                """;

        return jdbc.query(
                sql,
                parametrosFechaCorte(fechaCorte),
                RESUMEN_MAPPER
        );
    }


    // =========================================================
    // RESUMEN - ASOCIADO
    // =========================================================

    /**
     * Construye el resumen del Vector para los créditos
     * de un asociado que pertenecían a la población
     * del corte seleccionado.
     */
    public List<VectorComportamientoCorteResumenDTO>
    listarResumenPorPersona(
            LocalDate fechaCorte,
            Integer idDatosPersonal
    ) {

        String sql = SQL_RESUMEN_BASE + """

                WHERE u.id_datos_personal =
                      :idDatosPersonal

                ORDER BY
                    u.pagare_cartera,
                    u.id_cartera_credito
                """;

        MapSqlParameterSource params =
                parametrosFechaCorte(fechaCorte)
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        );

        return jdbc.query(
                sql,
                params,
                RESUMEN_MAPPER
        );
    }


    // =========================================================
    // RESUMEN - CRÉDITO
    // =========================================================

    /**
     * Obtiene el resumen de un crédito siempre que
     * perteneciera a la población del corte seleccionado.
     */
    public List<VectorComportamientoCorteResumenDTO>
    listarResumenPorCredito(
            LocalDate fechaCorte,
            Integer idCarteraCredito
    ) {

        String sql = SQL_RESUMEN_BASE + """

                WHERE u.id_cartera_credito =
                      :idCarteraCredito
                """;

        MapSqlParameterSource params =
                parametrosFechaCorte(fechaCorte)
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        );

        return jdbc.query(
                sql,
                params,
                RESUMEN_MAPPER
        );
    }


    // =========================================================
    // DETALLE - CARTERA DEL CORTE
    // =========================================================

    /**
     * Devuelve el Vector completo de la población
     * del corte seleccionado.
     *
     * Posición 1:
     * corte seleccionado.
     *
     * Posiciones 2..13:
     * máximo 12 cierres anteriores.
     */
    public List<VectorComportamientoCorteDetalleDTO>
    listarDetallePorCorte(
            LocalDate fechaCorte
    ) {

        String sql = SQL_DETALLE_BASE + """

                ORDER BY
                    v.id_agencia,
                    v.documento,
                    v.id_cartera_credito,
                    v.posicion_vector
                """;

        return jdbc.query(
                sql,
                parametrosFechaCorte(fechaCorte),
                DETALLE_MAPPER
        );
    }


    // =========================================================
    // DETALLE - ASOCIADO
    // =========================================================

    /**
     * Devuelve el Vector de los créditos de un asociado
     * pertenecientes a la población del corte seleccionado.
     */
    public List<VectorComportamientoCorteDetalleDTO>
    listarDetallePorPersona(
            LocalDate fechaCorte,
            Integer idDatosPersonal
    ) {

        String sql = SQL_DETALLE_BASE + """

                WHERE v.id_datos_personal =
                      :idDatosPersonal

                ORDER BY
                    v.id_cartera_credito,
                    v.posicion_vector
                """;

        MapSqlParameterSource params =
                parametrosFechaCorte(fechaCorte)
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        );

        return jdbc.query(
                sql,
                params,
                DETALLE_MAPPER
        );
    }


    // =========================================================
    // DETALLE - CRÉDITO
    // =========================================================

    /**
     * Devuelve el Vector de un crédito perteneciente
     * a la población del corte seleccionado.
     *
     * El resultado está ordenado:
     *
     * 1 = corte seleccionado.
     * 2..13 = cierres anteriores del más reciente
     *         al más antiguo.
     */
    public List<VectorComportamientoCorteDetalleDTO>
    listarDetallePorCredito(
            LocalDate fechaCorte,
            Integer idCarteraCredito
    ) {

        String sql = SQL_DETALLE_BASE + """

                WHERE v.id_cartera_credito =
                      :idCarteraCredito

                ORDER BY
                    v.posicion_vector
                """;

        MapSqlParameterSource params =
                parametrosFechaCorte(fechaCorte)
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        );

        return jdbc.query(
                sql,
                params,
                DETALLE_MAPPER
        );
    }


    // =========================================================
    // EXISTENCIA DEL CRÉDITO EN EL CORTE
    // =========================================================

    /**
     * Verifica si un crédito pertenecía a la población
     * del Vector en la fecha seleccionada.
     *
     * Regla poblacional:
     *
     * saldo_credito_fecha_corte > 0.
     */
    public boolean existeCreditoEnCorte(
            LocalDate fechaCorte,
            Integer idCarteraCredito
    ) {

        String sql = """
                SELECT EXISTS (

                    SELECT 1

                    FROM cartera.vw_cartera_resultados_mensuales_total h

                    WHERE h.fecha_corte =
                          :fechaCorte

                      AND h.id_cartera_credito =
                          :idCarteraCredito

                      AND COALESCE(
                              h.saldo_credito_fecha_corte,
                              0
                          ) > 0
                )
                """;

        MapSqlParameterSource params =
                parametrosFechaCorte(fechaCorte)
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        );

        Boolean existe =
                jdbc.queryForObject(
                        sql,
                        params,
                        Boolean.class
                );

        return Boolean.TRUE.equals(existe);
    }


    // =========================================================
    // SQL BASE - RESUMEN
    // =========================================================

    /**
     * Estructura:
     *
     * poblacion:
     *     créditos con saldo > 0 exactamente en el
     *     corte seleccionado.
     *
     * historico_numerado:
     *     cierres estrictamente anteriores al seleccionado,
     *     numerados desde el más reciente.
     *
     * historico_12:
     *     máximo 12 cierres anteriores.
     *
     * agregado:
     *     métricas históricas únicamente sobre esos
     *     12 cierres.
     *
     * El corte seleccionado se mantiene fuera del agregado
     * porque conceptualmente equivale al ACTUAL del
     * Vector de Comportamiento Actual.
     */
    private static final String SQL_RESUMEN_BASE = """
            WITH poblacion AS (

                SELECT DISTINCT ON (
                    h.id_cartera_credito
                )
                    h.*

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE h.fecha_corte =
                      :fechaCorte

                  AND COALESCE(
                          h.saldo_credito_fecha_corte,
                          0
                      ) > 0

                ORDER BY
                    h.id_cartera_credito,
                    h.id_cierre_cartera DESC,
                    h.id_cierre_cartera_credito DESC
            ),

            historico_numerado AS (

                SELECT
                    h.id_cartera_credito,
                    h.fecha_corte,

                    COALESCE(
                        h.dias_mora,
                        0
                    )::integer
                        AS dias_mora,

                    h.saldo_credito_fecha_corte,

                    ROW_NUMBER() OVER (
                        PARTITION BY
                            h.id_cartera_credito

                        ORDER BY
                            h.fecha_corte DESC,
                            h.id_cierre_cartera DESC,
                            h.id_cierre_cartera_credito DESC
                    )
                        AS numero_historico

                FROM cartera.vw_cartera_resultados_mensuales_total h

                INNER JOIN poblacion p
                        ON p.id_cartera_credito =
                           h.id_cartera_credito

                WHERE h.fecha_corte <
                      :fechaCorte
            ),

            historico_12 AS (

                SELECT
                    h.*

                FROM historico_numerado h

                WHERE h.numero_historico <= 12
            ),

            agregado AS (

                SELECT
                    h.id_cartera_credito,

                    MIN(h.fecha_corte)
                        AS primer_corte,

                    MAX(h.fecha_corte)
                        AS ultimo_corte,

                    COUNT(*)::integer
                        AS cantidad_cortes_observados,

                    MAX(
                        COALESCE(
                            h.dias_mora,
                            0
                        )
                    )::integer
                        AS mora_maxima,

                    COUNT(*) FILTER (
                        WHERE COALESCE(h.dias_mora, 0) = 0
                    )::integer
                        AS cantidad_cortes_al_dia,

                    COUNT(*) FILTER (
                        WHERE COALESCE(h.dias_mora, 0) > 0
                    )::integer
                        AS cantidad_cortes_con_mora,

                    COUNT(*) FILTER (
                        WHERE h.dias_mora BETWEEN 1 AND 30
                    )::integer
                        AS cantidad_mora_1_30,

                    COUNT(*) FILTER (
                        WHERE h.dias_mora BETWEEN 31 AND 60
                    )::integer
                        AS cantidad_mora_31_60,

                    COUNT(*) FILTER (
                        WHERE h.dias_mora BETWEEN 61 AND 90
                    )::integer
                        AS cantidad_mora_61_90,

                    COUNT(*) FILTER (
                        WHERE h.dias_mora BETWEEN 91 AND 120
                    )::integer
                        AS cantidad_mora_91_120,

                    COUNT(*) FILTER (
                        WHERE h.dias_mora BETWEEN 121 AND 150
                    )::integer
                        AS cantidad_mora_121_150,

                    COUNT(*) FILTER (
                        WHERE h.dias_mora BETWEEN 151 AND 180
                    )::integer
                        AS cantidad_mora_151_180,

                    COUNT(*) FILTER (
                        WHERE h.dias_mora BETWEEN 181 AND 360
                    )::integer
                        AS cantidad_mora_181_360,

                    COUNT(*) FILTER (
                        WHERE h.dias_mora > 360
                    )::integer
                        AS cantidad_mora_mayor_360,

                    (
                        ARRAY_AGG(
                            h.saldo_credito_fecha_corte
                            ORDER BY
                                h.fecha_corte ASC
                        )
                    )[1]
                        AS saldo_primer_corte

                FROM historico_12 h

                GROUP BY
                    h.id_cartera_credito
            )

            SELECT
                u.id_cartera_credito,
                u.id_agencia,
                u.id_linea_credito,
                u.codigo_linea_credito,
                u.nombre_linea_credito,
                u.pagare_cartera,
                u.id_datos_personal,
                u.tipo_documento,
                u.documento,
                u.nombre_completo,

                dp.telefono,

                dp.celular_uno
                    AS celular,

                dp.correo_personal
                    AS correo,

                CAST(:fechaCorte AS date)
                    AS fecha_corte,

                a.primer_corte,
                a.ultimo_corte,

                COALESCE(
                    a.cantidad_cortes_observados,
                    0
                )::integer
                    AS cantidad_cortes_observados,

                COALESCE(
                    u.dias_mora,
                    0
                )::integer
                    AS mora_ultimo_corte,

                COALESCE(
                    a.mora_maxima,
                    0
                )::integer
                    AS mora_maxima,

                COALESCE(
                    a.cantidad_cortes_al_dia,
                    0
                )::integer
                    AS cantidad_cortes_al_dia,

                COALESCE(
                    a.cantidad_cortes_con_mora,
                    0
                )::integer
                    AS cantidad_cortes_con_mora,

                COALESCE(
                    a.cantidad_mora_1_30,
                    0
                )::integer
                    AS cantidadMora1_30,

                COALESCE(
                    a.cantidad_mora_31_60,
                    0
                )::integer
                    AS cantidadMora31_60,

                COALESCE(
                    a.cantidad_mora_61_90,
                    0
                )::integer
                    AS cantidadMora61_90,

                COALESCE(
                    a.cantidad_mora_91_120,
                    0
                )::integer
                    AS cantidadMora91_120,

                COALESCE(
                    a.cantidad_mora_121_150,
                    0
                )::integer
                    AS cantidadMora121_150,

                COALESCE(
                    a.cantidad_mora_151_180,
                    0
                )::integer
                    AS cantidadMora151_180,

                COALESCE(
                    a.cantidad_mora_181_360,
                    0
                )::integer
                    AS cantidadMora181_360,

                COALESCE(
                    a.cantidad_mora_mayor_360,
                    0
                )::integer
                    AS cantidadMoraMayor360,

                CASE
                    WHEN COALESCE(
                             a.cantidad_cortes_observados,
                             0
                         ) > 0
                    THEN ROUND(
                        (
                            COALESCE(
                                a.cantidad_cortes_con_mora,
                                0
                            )::numeric
                            /
                            a.cantidad_cortes_observados::numeric
                        ) * 100,
                        2
                    )
                    ELSE 0
                END
                    AS pbb_mora,

                u.fecha_desembolso,

                u.valor_inicial_credito,
                u.valor_desembolsado,

                a.saldo_primer_corte,

                u.saldo_credito_fecha_corte
                    AS saldo_ultimo_corte,

                (
                    COALESCE(
                        u.saldo_credito_fecha_corte,
                        0
                    )
                    -
                    COALESCE(
                        a.saldo_primer_corte,
                        0
                    )
                )
                    AS variacion_saldo_periodo,

                CASE
                    WHEN COALESCE(
                             u.valor_desembolsado,
                             0
                         ) > 0
                    THEN ROUND(
                        (
                            COALESCE(
                                u.saldo_credito_fecha_corte,
                                0
                            )
                            /
                            u.valor_desembolsado
                        ) * 100,
                        2
                    )
                    ELSE NULL
                END
                    AS severidad,

                CASE
                    WHEN COALESCE(
                             u.valor_desembolsado,
                             0
                         ) <= 0
                        THEN 'SIN INFORMACION'

                    WHEN (
                        COALESCE(
                            u.saldo_credito_fecha_corte,
                            0
                        )
                        /
                        u.valor_desembolsado
                        * 100
                    ) <= 10
                        THEN 'SEVERIDAD 0-10'

                    WHEN (
                        COALESCE(
                            u.saldo_credito_fecha_corte,
                            0
                        )
                        /
                        u.valor_desembolsado
                        * 100
                    ) <= 25
                        THEN 'SEVERIDAD 10-25'

                    WHEN (
                        COALESCE(
                            u.saldo_credito_fecha_corte,
                            0
                        )
                        /
                        u.valor_desembolsado
                        * 100
                    ) <= 50
                        THEN 'SEVERIDAD 25-50'

                    WHEN (
                        COALESCE(
                            u.saldo_credito_fecha_corte,
                            0
                        )
                        /
                        u.valor_desembolsado
                        * 100
                    ) <= 75
                        THEN 'SEVERIDAD 50-75'

                    ELSE 'SEVERIDAD 75-100'
                END
                    AS rango_severidad,

                u.codigo_estado_cartera,
                u.descripcion_estado_cartera,

                u.codigo_estado_juridico,
                u.descripcion_estado_juridico,

                u.codigo_clasificacion_credito,
                u.descripcion_clasificacion_credito,

                u.edad_riesgo_inicial_resultado,
                u.edad_de_mora_resultado,
                u.edad_de_riesgo_resultado,
                u.edad_de_pe_resultado,
                u.edad_de_homologacion_resultado,
                u.edad_contable_resultado,

                u.codigo_metodo_calculo,

                u.vea,
                u.pi,
                u.pdi,
                u.perdida_esperada,

                u.deterioro_capital,
                u.deterioro_intereses,
                u.deterioro_otros,
                u.deterioro_total,

                (
                    COALESCE(
                        a.cantidad_cortes_con_mora,
                        0
                    ) > 0
                )
                    AS tuvo_mora_periodo,

                (
                    COALESCE(
                        u.dias_mora,
                        0
                    ) > 0
                )
                    AS esta_en_mora_ultimo_corte,

                (
                    COALESCE(
                        a.mora_maxima,
                        0
                    ) > 90
                )
                    AS "tuvoMoraMayor90",

                u.id_cierre_cartera,
                u.id_cierre_cartera_credito,
                u.id_cierre_cartera_resultado

            FROM poblacion u

            LEFT JOIN agregado a
                   ON a.id_cartera_credito =
                      u.id_cartera_credito

            LEFT JOIN reporting.vw_datos_personales_operativa dp
                   ON dp.id_datos_personal =
                      u.id_datos_personal
            """;


    // =========================================================
    // SQL BASE - DETALLE
    // =========================================================

    /**
     * Construye el Vector por Corte:
     *
     * posición 1:
     *     corte seleccionado, tratado como ACTUAL.
     *
     * posiciones 2..13:
     *     máximo 12 cierres anteriores.
     *
     * No utiliza información del maestro actual
     * de cartera.
     */
    private static final String SQL_DETALLE_BASE = """
            WITH poblacion AS (

                SELECT DISTINCT ON (
                    h.id_cartera_credito
                )
                    h.*

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE h.fecha_corte =
                      :fechaCorte

                  AND COALESCE(
                          h.saldo_credito_fecha_corte,
                          0
                      ) > 0

                ORDER BY
                    h.id_cartera_credito,
                    h.id_cierre_cartera DESC,
                    h.id_cierre_cartera_credito DESC
            ),

            historico_numerado AS (

                SELECT
                    h.*,

                    ROW_NUMBER() OVER (
                        PARTITION BY
                            h.id_cartera_credito

                        ORDER BY
                            h.fecha_corte DESC,
                            h.id_cierre_cartera DESC,
                            h.id_cierre_cartera_credito DESC
                    )
                        AS numero_historico

                FROM cartera.vw_cartera_resultados_mensuales_total h

                INNER JOIN poblacion p
                        ON p.id_cartera_credito =
                           h.id_cartera_credito

                WHERE h.fecha_corte <
                      :fechaCorte
            ),

            historico_12 AS (

                SELECT
                    h.*

                FROM historico_numerado h

                WHERE h.numero_historico <= 12
            ),

            vector_referencia AS (

                SELECT
                    1::bigint
                        AS posicion_vector,

                    'ACTUAL'::text
                        AS tipo_posicion,

                    'ACTUAL'::text
                        AS periodo_vector,

                    p.fecha_corte
                        AS fecha_referencia,

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

                    CAST(:fechaCorte AS date)
                        AS fecha_corte_seleccionado,

                    p.id_cierre_cartera,
                    p.id_cierre_cartera_credito,
                    p.id_cierre_cartera_resultado,

                    p.fecha_corte,
                    p.anio_corte,
                    p.mes_corte,
                    p.periodo_corte,

                    p.codigo_estado_cartera,
                    p.descripcion_estado_cartera,

                    p.codigo_estado_juridico,
                    p.descripcion_estado_juridico,

                    p.codigo_clasificacion_credito,
                    p.descripcion_clasificacion_credito,

                    COALESCE(
                        p.dias_mora,
                        0
                    )::integer
                        AS dias_mora,

                    CASE
                        WHEN COALESCE(p.dias_mora, 0) = 0
                            THEN 'AL DIA'

                        WHEN p.dias_mora BETWEEN 1 AND 30
                            THEN 'MORA 1-30'

                        WHEN p.dias_mora BETWEEN 31 AND 60
                            THEN 'MORA 31-60'

                        WHEN p.dias_mora BETWEEN 61 AND 90
                            THEN 'MORA 61-90'

                        WHEN p.dias_mora BETWEEN 91 AND 120
                            THEN 'MORA 91-120'

                        WHEN p.dias_mora BETWEEN 121 AND 150
                            THEN 'MORA 121-150'

                        WHEN p.dias_mora BETWEEN 151 AND 180
                            THEN 'MORA 151-180'

                        WHEN p.dias_mora BETWEEN 181 AND 360
                            THEN 'MORA 181-360'

                        WHEN p.dias_mora > 360
                            THEN 'MORA > 360'

                        ELSE 'SIN INFORMACION'
                    END
                        AS rango_mora,

                    CASE
                        WHEN COALESCE(p.dias_mora, 0) = 0
                            THEN 0

                        WHEN p.dias_mora BETWEEN 1 AND 30
                            THEN 1

                        WHEN p.dias_mora BETWEEN 31 AND 60
                            THEN 2

                        WHEN p.dias_mora BETWEEN 61 AND 90
                            THEN 3

                        WHEN p.dias_mora BETWEEN 91 AND 120
                            THEN 4

                        WHEN p.dias_mora BETWEEN 121 AND 150
                            THEN 5

                        WHEN p.dias_mora BETWEEN 151 AND 180
                            THEN 6

                        WHEN p.dias_mora BETWEEN 181 AND 360
                            THEN 7

                        WHEN p.dias_mora > 360
                            THEN 8

                        ELSE 9
                    END
                        AS orden_rango_mora,

                    (
                        COALESCE(
                            p.dias_mora,
                            0
                        ) > 0
                    )
                        AS tiene_mora,

                    p.edad_riesgo_inicial_resultado,
                    p.edad_de_mora_resultado,
                    p.edad_de_riesgo_resultado,
                    p.edad_de_pe_resultado,
                    p.edad_de_homologacion_resultado,
                    p.edad_contable_resultado,

                    p.valor_inicial_credito,
                    p.valor_desembolsado,

                    p.saldo_actual_fotografia,
                    p.saldo_actual_resultado,
                    p.saldo_credito_fecha_corte,

                    p.codigo_metodo_calculo,

                    p.vea,
                    p.pi,
                    p.pdi,
                    p.perdida_esperada,

                    p.deterioro_capital,
                    p.deterioro_intereses,
                    p.deterioro_otros,
                    p.deterioro_total,

                    p.saldo_aportes_fecha_corte,
                    p.porcentaje_aportes_credito,
                    p.valor_aportes_credito,

                    p.cantidad_bienes_garantia,
                    p.valor_garantias_total,
                    p.porcentaje_garantias_credito,
                    p.valor_garantias_credito

                FROM poblacion p
            ),

            vector_historico AS (

                SELECT
                    (
                        h.numero_historico + 1
                    )::bigint
                        AS posicion_vector,

                    'CIERRE'::text
                        AS tipo_posicion,

                    h.periodo_corte::text
                        AS periodo_vector,

                    h.fecha_corte
                        AS fecha_referencia,

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

                    CAST(:fechaCorte AS date)
                        AS fecha_corte_seleccionado,

                    h.id_cierre_cartera,
                    h.id_cierre_cartera_credito,
                    h.id_cierre_cartera_resultado,

                    h.fecha_corte,
                    h.anio_corte,
                    h.mes_corte,
                    h.periodo_corte,

                    h.codigo_estado_cartera,
                    h.descripcion_estado_cartera,

                    h.codigo_estado_juridico,
                    h.descripcion_estado_juridico,

                    h.codigo_clasificacion_credito,
                    h.descripcion_clasificacion_credito,

                    COALESCE(
                        h.dias_mora,
                        0
                    )::integer
                        AS dias_mora,

                    CASE
                        WHEN COALESCE(h.dias_mora, 0) = 0
                            THEN 'AL DIA'

                        WHEN h.dias_mora BETWEEN 1 AND 30
                            THEN 'MORA 1-30'

                        WHEN h.dias_mora BETWEEN 31 AND 60
                            THEN 'MORA 31-60'

                        WHEN h.dias_mora BETWEEN 61 AND 90
                            THEN 'MORA 61-90'

                        WHEN h.dias_mora BETWEEN 91 AND 120
                            THEN 'MORA 91-120'

                        WHEN h.dias_mora BETWEEN 121 AND 150
                            THEN 'MORA 121-150'

                        WHEN h.dias_mora BETWEEN 151 AND 180
                            THEN 'MORA 151-180'

                        WHEN h.dias_mora BETWEEN 181 AND 360
                            THEN 'MORA 181-360'

                        WHEN h.dias_mora > 360
                            THEN 'MORA > 360'

                        ELSE 'SIN INFORMACION'
                    END
                        AS rango_mora,

                    CASE
                        WHEN COALESCE(h.dias_mora, 0) = 0
                            THEN 0

                        WHEN h.dias_mora BETWEEN 1 AND 30
                            THEN 1

                        WHEN h.dias_mora BETWEEN 31 AND 60
                            THEN 2

                        WHEN h.dias_mora BETWEEN 61 AND 90
                            THEN 3

                        WHEN h.dias_mora BETWEEN 91 AND 120
                            THEN 4

                        WHEN h.dias_mora BETWEEN 121 AND 150
                            THEN 5

                        WHEN h.dias_mora BETWEEN 151 AND 180
                            THEN 6

                        WHEN h.dias_mora BETWEEN 181 AND 360
                            THEN 7

                        WHEN h.dias_mora > 360
                            THEN 8

                        ELSE 9
                    END
                        AS orden_rango_mora,

                    (
                        COALESCE(
                            h.dias_mora,
                            0
                        ) > 0
                    )
                        AS tiene_mora,

                    h.edad_riesgo_inicial_resultado,
                    h.edad_de_mora_resultado,
                    h.edad_de_riesgo_resultado,
                    h.edad_de_pe_resultado,
                    h.edad_de_homologacion_resultado,
                    h.edad_contable_resultado,

                    h.valor_inicial_credito,
                    h.valor_desembolsado,

                    h.saldo_actual_fotografia,
                    h.saldo_actual_resultado,
                    h.saldo_credito_fecha_corte,

                    h.codigo_metodo_calculo,

                    h.vea,
                    h.pi,
                    h.pdi,
                    h.perdida_esperada,

                    h.deterioro_capital,
                    h.deterioro_intereses,
                    h.deterioro_otros,
                    h.deterioro_total,

                    h.saldo_aportes_fecha_corte,
                    h.porcentaje_aportes_credito,
                    h.valor_aportes_credito,

                    h.cantidad_bienes_garantia,
                    h.valor_garantias_total,
                    h.porcentaje_garantias_credito,
                    h.valor_garantias_credito

                FROM historico_12 h
            ),

            vector AS (

                SELECT *
                FROM vector_referencia

                UNION ALL

                SELECT *
                FROM vector_historico
            )

            SELECT
                v.*

            FROM vector v
            """;


    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource parametrosFechaCorte(
            LocalDate fechaCorte
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "fechaCorte",
                        fechaCorte
                );
    }


    // =========================================================
    // UTILIDADES
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
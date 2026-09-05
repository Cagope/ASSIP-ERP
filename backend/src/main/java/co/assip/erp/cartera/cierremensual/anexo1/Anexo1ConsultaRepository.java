package co.assip.erp.cartera.cierremensual.anexo1;

import co.assip.erp.cartera.cierremensual.anexo1.dto.DetalleAnexo1DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import co.assip.erp.cartera.cierremensual.anexo1.dto.ResumenAnexo1DTO;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class Anexo1ConsultaRepository {

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // OBTENER DETALLE ANEXO 1
    //
    // CONSULTA EXCLUSIVAMENTE DE LECTURA.
    //
    // No ejecuta cálculos.
    // No actualiza resultados.
    // No modifica la fotografía del cierre.
    //
    // Los porcentajes utilizados para presentar el detalle
    // se reconstruyen aplicando las mismas reglas utilizadas
    // por el motor de Anexo 1:
    //
    // - porcentaje normal de deterioro
    // - porcentaje especial de deterioro
    // - porcentaje de aplicación de garantía
    //
    // Esto permite consultar de manera auditable el resultado
    // ya almacenado en:
    //
    // cartera.cierres_cartera_resultados
    //
    // =========================================================

    public List<DetalleAnexo1DTO> obtenerDetalleAnexo1(
            Integer idCierreCartera
    ) {

        String sql = """
                WITH detalle AS
                (
                    SELECT
                        c.id_cierre_cartera,
                        c.fecha_corte,

                        f.id_cartera_credito,
                        f.id_cierre_cartera_credito,
                        r.id_cierre_cartera_resultado,
                        f.id_agencia,

                        f.id_datos_personal,

                        f.pagare_cartera,

                        f.tipo_documento,
                        f.documento,

                        f.nombres,
                        f.primer_apellido,
                        f.segundo_apellido,

                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(f.nombres), ''),
                            NULLIF(TRIM(f.primer_apellido), ''),
                            NULLIF(TRIM(f.segundo_apellido), '')
                        ) AS nombre_completo,

                        f.id_linea_credito,
                        f.codigo_linea_credito,
                        f.nombre_linea_credito,

                        f.codigo_clasificacion_credito,
                        f.descripcion_clasificacion_credito,

                        f.codigo_garantia_credito,
                        f.descripcion_garantia_credito,
                        f.tipo_garantia,

                        TRIM(hv.tipo_persona)
                            AS tipo_persona,

                        r.dias_mora,

                        r.edad_riesgo_inicial,
                        r.edad_de_mora,
                        r.edad_de_riesgo,
                        r.edad_contable,

                        COALESCE(
                            r.saldo_actual,
                            0
                        ) AS saldo_capital,

                        COALESCE(
                            f.tasa_nominal_anual,
                            0
                        ) AS tasa_nominal_anual,

                        COALESCE(
                            r.saldo_intereses_causados,
                            0
                        ) AS saldo_intereses_causados,

                        COALESCE(
                            r.saldo_intereses_contingentes,
                            0
                        ) AS saldo_intereses_contingentes,

                        COALESCE(
                            r.saldo_aportes_fecha_corte,
                            0
                        ) AS saldo_aportes_fecha_corte,

                        COALESCE(
                            r.porcentaje_aportes_credito,
                            0
                        ) AS porcentaje_aportes_credito,

                        COALESCE(
                            r.valor_aportes_credito,
                            0
                        ) AS valor_aportes_credito,

                        COALESCE(
                            r.cantidad_bienes_garantia,
                            0
                        ) AS cantidad_bienes_garantia,

                        COALESCE(
                            r.valor_garantias_total,
                            0
                        ) AS valor_garantias_total,

                        COALESCE(
                            r.porcentaje_garantias_credito,
                            0
                        ) AS porcentaje_garantias_credito,

                        COALESCE(
                            r.valor_garantias_credito,
                            0
                        ) AS valor_garantias_credito,

                        COALESCE(
                            pg.porcentaje_aplicacion,
                            0
                        ) AS porcentaje_aplicacion_garantia,

                        ROUND(
                            COALESCE(
                                r.valor_garantias_credito,
                                0
                            )
                            *
                            COALESCE(
                                pg.porcentaje_aplicacion,
                                0
                            )
                            / 100,
                            0
                        ) AS valor_garantia_reconocida,

                        COALESCE(
                            pde.porcentaje_deterioro_capital,
                            pd.porcentaje_deterioro_capital
                        ) AS porcentaje_deterioro_capital,

                        GREATEST(
                            COALESCE(
                                r.saldo_actual,
                                0
                            )
                            -
                            COALESCE(
                                r.valor_aportes_credito,
                                0
                            )
                            -
                            ROUND(
                                COALESCE(
                                    r.valor_garantias_credito,
                                    0
                                )
                                *
                                COALESCE(
                                    pg.porcentaje_aplicacion,
                                    0
                                )
                                / 100,
                                0
                            ),
                            0
                        ) AS base_deterioro_capital,

                        COALESCE(
                            r.deterioro_capital,
                            0
                        ) AS deterioro_capital,

                        COALESCE(
                            pde.porcentaje_deterioro_intereses,
                            pd.porcentaje_deterioro_intereses
                        ) AS porcentaje_deterioro_intereses,

                        COALESCE(
                            r.deterioro_intereses,
                            0
                        ) AS deterioro_intereses,

                        r.codigo_metodo_calculo

                    FROM cartera.cierres_cartera_resultados r

                    INNER JOIN cartera.cierres_cartera_creditos f
                        ON f.id_cierre_cartera_credito =
                           r.id_cierre_cartera_credito

                    INNER JOIN cartera.cierres_cartera c
                        ON c.id_cierre_cartera =
                           r.id_cierre_cartera

                    /*
                     * FOTOGRAFÍA DE HOJA DE VIDA
                     *
                     * El tipo de persona se obtiene de la
                     * fotografía correspondiente a la misma
                     * fecha de corte del cierre de cartera.
                     */

                    INNER JOIN hoja_vida.cierres_hoja_vida chv
                        ON chv.fecha_corte =
                           c.fecha_corte

                    INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                        ON hv.id_cierre_hoja_vida =
                           chv.id_cierre_hoja_vida

                       AND hv.id_datos_personal =
                           f.id_datos_personal

                    /*
                     * PORCENTAJE NORMAL
                     *
                     * Se determina usando:
                     *
                     * - clasificación del crédito
                     * - tipo de persona
                     * - edad contable
                     * - vigencia a fecha de corte
                     */

                    INNER JOIN cartera.porcentajes_deterioro pd
                        ON pd.codigo_clasificacion_credito =
                           f.codigo_clasificacion_credito

                       AND pd.id_tipo_persona =
                           TRIM(hv.tipo_persona)::integer

                       AND pd.codigo_clasificacion_deterioro =
                           r.edad_contable

                       AND pd.activo = true

                       AND pd.vigencia_desde <=
                           c.fecha_corte

                       AND (
                            pd.vigencia_hasta IS NULL
                            OR pd.vigencia_hasta >=
                               c.fecha_corte
                       )

                    /*
                     * PORCENTAJE ESPECIAL
                     *
                     * Tiene precedencia sobre el porcentaje normal.
                     *
                     * Se determina usando:
                     *
                     * - clasificación del crédito
                     * - tipo de persona
                     * - edad de mora
                     * - rango de días de mora
                     * - vigencia a fecha de corte
                     *
                     * LATERAL + LIMIT 1 evita multiplicar el crédito
                     * ante una eventual parametrización solapada.
                     */

                    LEFT JOIN LATERAL
                    (
                        SELECT
                            pe.porcentaje_deterioro_capital,
                            pe.porcentaje_deterioro_intereses

                        FROM cartera.porcentajes_deterioro_especial pe

                        WHERE pe.codigo_clasificacion_credito =
                              f.codigo_clasificacion_credito

                          AND pe.id_tipo_persona =
                              TRIM(hv.tipo_persona)::smallint

                          AND pe.codigo_edad_mora =
                              r.edad_de_mora

                          AND r.dias_mora BETWEEN
                              pe.dias_desde
                              AND pe.dias_hasta

                          AND pe.activo = true

                          AND pe.vigencia_desde <=
                              c.fecha_corte

                          AND (
                               pe.vigencia_hasta IS NULL
                               OR pe.vigencia_hasta >=
                                  c.fecha_corte
                          )

                        ORDER BY
                            pe.dias_desde DESC,
                            pe.id_porcentaje_deterioro_especial DESC

                        LIMIT 1

                    ) pde ON true

                    /*
                     * PORCENTAJE DE APLICACIÓN DE GARANTÍA
                     *
                     * Se determina usando:
                     *
                     * - código de garantía
                     * - días de mora
                     * - vigencia a fecha de corte
                     *
                     * Se utiliza exactamente la misma selección
                     * empleada por el motor de Anexo 1.
                     */

                    LEFT JOIN LATERAL
                    (
                        SELECT
                            p.porcentaje_aplicacion

                        FROM cartera.porcentajes_aplicacion_garantia p

                        WHERE p.codigo_garantia_credito =
                              f.codigo_garantia_credito

                          AND r.dias_mora <=
                              p.dias_hasta

                          AND p.activo = true

                          AND p.vigencia_desde <=
                              c.fecha_corte

                          AND (
                               p.vigencia_hasta IS NULL
                               OR p.vigencia_hasta >=
                                  c.fecha_corte
                          )

                        ORDER BY
                            p.dias_hasta

                        LIMIT 1

                    ) pg ON true

                    WHERE r.id_cierre_cartera =
                          :idCierreCartera
                )

                SELECT
                    *

                FROM detalle

                ORDER BY
                    id_agencia,
                    codigo_linea_credito,
                    pagare_cartera,
                    id_cierre_cartera_credito
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return jdbc.query(
                sql,
                parametros,
                (rs, rowNum) ->
                        new DetalleAnexo1DTO(

                                // =====================================
                                // CIERRE
                                // =====================================

                                rs.getObject(
                                        "id_cierre_cartera",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "fecha_corte",
                                        java.time.LocalDate.class
                                ),

                                // =====================================
                                // IDENTIFICACIÓN
                                // =====================================

                                rs.getObject(
                                        "id_cartera_credito",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "id_cierre_cartera_credito",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "id_cierre_cartera_resultado",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "id_agencia",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "id_datos_personal",
                                        Integer.class
                                ),

                                rs.getString(
                                        "pagare_cartera"
                                ),

                                rs.getString(
                                        "tipo_documento"
                                ),

                                rs.getString(
                                        "documento"
                                ),

                                rs.getString(
                                        "nombres"
                                ),

                                rs.getString(
                                        "primer_apellido"
                                ),

                                rs.getString(
                                        "segundo_apellido"
                                ),

                                rs.getString(
                                        "nombre_completo"
                                ),

                                // =====================================
                                // LÍNEA
                                // =====================================

                                rs.getObject(
                                        "id_linea_credito",
                                        Integer.class
                                ),

                                rs.getString(
                                        "codigo_linea_credito"
                                ),

                                rs.getString(
                                        "nombre_linea_credito"
                                ),

                                // =====================================
                                // CLASIFICACIÓN
                                // =====================================

                                rs.getString(
                                        "codigo_clasificacion_credito"
                                ),

                                rs.getString(
                                        "descripcion_clasificacion_credito"
                                ),

                                // =====================================
                                // GARANTÍA
                                // =====================================

                                rs.getString(
                                        "codigo_garantia_credito"
                                ),

                                rs.getString(
                                        "descripcion_garantia_credito"
                                ),

                                rs.getString(
                                        "tipo_garantia"
                                ),

                                // =====================================
                                // TIPO PERSONA
                                // =====================================

                                rs.getString(
                                        "tipo_persona"
                                ),

                                // =====================================
                                // MORA Y EDADES
                                // =====================================

                                rs.getObject(
                                        "dias_mora",
                                        Integer.class
                                ),

                                rs.getString(
                                        "edad_riesgo_inicial"
                                ),

                                rs.getString(
                                        "edad_de_mora"
                                ),

                                rs.getString(
                                        "edad_de_riesgo"
                                ),

                                rs.getString(
                                        "edad_contable"
                                ),

                                // =====================================
                                // SALDOS
                                // =====================================

                                rs.getBigDecimal(
                                        "saldo_capital"
                                ),

                                rs.getBigDecimal(
                                        "tasa_nominal_anual"
                                ),

                                rs.getBigDecimal(
                                        "saldo_intereses_causados"
                                ),

                                rs.getBigDecimal(
                                        "saldo_intereses_contingentes"
                                ),

                                // =====================================
                                // APORTES
                                // =====================================

                                rs.getBigDecimal(
                                        "saldo_aportes_fecha_corte"
                                ),

                                rs.getBigDecimal(
                                        "porcentaje_aportes_credito"
                                ),

                                rs.getBigDecimal(
                                        "valor_aportes_credito"
                                ),

                                // =====================================
                                // GARANTÍAS PRORRATEADAS
                                // =====================================

                                rs.getObject(
                                        "cantidad_bienes_garantia",
                                        Integer.class
                                ),

                                rs.getBigDecimal(
                                        "valor_garantias_total"
                                ),

                                rs.getBigDecimal(
                                        "porcentaje_garantias_credito"
                                ),

                                rs.getBigDecimal(
                                        "valor_garantias_credito"
                                ),

                                // =====================================
                                // GARANTÍA RECONOCIDA
                                // =====================================

                                rs.getBigDecimal(
                                        "porcentaje_aplicacion_garantia"
                                ),

                                rs.getBigDecimal(
                                        "valor_garantia_reconocida"
                                ),

                                // =====================================
                                // DETERIORO CAPITAL
                                // =====================================

                                rs.getBigDecimal(
                                        "porcentaje_deterioro_capital"
                                ),

                                rs.getBigDecimal(
                                        "base_deterioro_capital"
                                ),

                                rs.getBigDecimal(
                                        "deterioro_capital"
                                ),

                                // =====================================
                                // DETERIORO INTERESES
                                // =====================================

                                rs.getBigDecimal(
                                        "porcentaje_deterioro_intereses"
                                ),

                                rs.getBigDecimal(
                                        "deterioro_intereses"
                                ),

                                // =====================================
                                // CONTROL
                                // =====================================

                                rs.getString(
                                        "codigo_metodo_calculo"
                                )
                        )
        );
    }

    // =========================================================
    // OBTENER RESUMEN ANEXO 1
    //
    // CONSULTA EXCLUSIVAMENTE DE LECTURA.
    //
    // Resume los resultados ya calculados y almacenados para
    // el cierre mensual.
    //
    // Incluye:
    //
    // - población procesada
    // - saldos
    // - distribución por edad contable
    // - aportes aplicados
    // - garantías asignadas
    // - garantías reconocidas
    // - base de deterioro de capital
    // - deterioro de capital
    // - deterioro de intereses
    // - deterioro total
    //
    // La garantía reconocida se reconstruye utilizando la misma
    // parametrización aplicada por el motor de Anexo 1.
    // =========================================================

    public ResumenAnexo1DTO obtenerResumenAnexo1(
            Integer idCierreCartera
    ) {

        String sql = """
            WITH base AS
            (
                SELECT
                    c.id_cierre_cartera,
                    c.fecha_corte,

                    r.edad_contable,

                    COALESCE(
                        r.saldo_actual,
                        0
                    ) AS saldo_capital,

                    COALESCE(
                        r.saldo_intereses_causados,
                        0
                    ) AS saldo_intereses_causados,

                    COALESCE(
                        r.valor_aportes_credito,
                        0
                    ) AS valor_aportes_credito,

                    COALESCE(
                        r.valor_garantias_credito,
                        0
                    ) AS valor_garantias_credito,

                    ROUND(
                        COALESCE(
                            r.valor_garantias_credito,
                            0
                        )
                        *
                        COALESCE(
                            pg.porcentaje_aplicacion,
                            0
                        )
                        / 100,
                        0
                    ) AS valor_garantia_reconocida,

                    GREATEST(
                        COALESCE(
                            r.saldo_actual,
                            0
                        )
                        -
                        COALESCE(
                            r.valor_aportes_credito,
                            0
                        )
                        -
                        ROUND(
                            COALESCE(
                                r.valor_garantias_credito,
                                0
                            )
                            *
                            COALESCE(
                                pg.porcentaje_aplicacion,
                                0
                            )
                            / 100,
                            0
                        ),
                        0
                    ) AS base_deterioro_capital,

                    COALESCE(
                        r.deterioro_capital,
                        0
                    ) AS deterioro_capital,

                    COALESCE(
                        r.deterioro_intereses,
                        0
                    ) AS deterioro_intereses

                FROM cartera.cierres_cartera_resultados r

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       r.id_cierre_cartera_credito

                INNER JOIN cartera.cierres_cartera c
                    ON c.id_cierre_cartera =
                       r.id_cierre_cartera

                /*
                 * PORCENTAJE DE APLICACIÓN DE GARANTÍA
                 *
                 * Se selecciona según:
                 *
                 * - código de garantía
                 * - días de mora
                 * - vigencia a fecha de corte
                 */
                LEFT JOIN LATERAL
                (
                    SELECT
                        p.porcentaje_aplicacion

                    FROM cartera.porcentajes_aplicacion_garantia p

                    WHERE p.codigo_garantia_credito =
                          f.codigo_garantia_credito

                      AND r.dias_mora <=
                          p.dias_hasta

                      AND p.activo = true

                      AND p.vigencia_desde <=
                          c.fecha_corte

                      AND (
                           p.vigencia_hasta IS NULL
                           OR p.vigencia_hasta >=
                              c.fecha_corte
                      )

                    ORDER BY
                        p.dias_hasta

                    LIMIT 1

                ) pg ON true

                WHERE r.id_cierre_cartera =
                      :idCierreCartera
            )

            SELECT
                id_cierre_cartera,
                fecha_corte,

                COUNT(*)::integer
                    AS cantidad_creditos,

                COALESCE(
                    SUM(saldo_capital),
                    0
                ) AS saldo_capital,

                COALESCE(
                    SUM(saldo_intereses_causados),
                    0
                ) AS saldo_intereses_causados,

                COUNT(*) FILTER (
                    WHERE edad_contable = 'A'
                )::integer AS cantidad_edad_a,

                COUNT(*) FILTER (
                    WHERE edad_contable = 'B'
                )::integer AS cantidad_edad_b,

                COUNT(*) FILTER (
                    WHERE edad_contable = 'C'
                )::integer AS cantidad_edad_c,

                COUNT(*) FILTER (
                    WHERE edad_contable = 'D'
                )::integer AS cantidad_edad_d,

                COUNT(*) FILTER (
                    WHERE edad_contable = 'E'
                )::integer AS cantidad_edad_e,

                COALESCE(
                    SUM(valor_aportes_credito),
                    0
                ) AS valor_aportes_aplicados,

                COALESCE(
                    SUM(valor_garantias_credito),
                    0
                ) AS valor_garantias_asignadas,

                COALESCE(
                    SUM(valor_garantia_reconocida),
                    0
                ) AS valor_garantias_reconocidas,

                COALESCE(
                    SUM(base_deterioro_capital),
                    0
                ) AS base_deterioro_capital,

                COALESCE(
                    SUM(deterioro_capital),
                    0
                ) AS deterioro_capital,

                COALESCE(
                    SUM(deterioro_intereses),
                    0
                ) AS deterioro_intereses,

                COALESCE(
                    SUM(deterioro_capital),
                    0
                )
                +
                COALESCE(
                    SUM(deterioro_intereses),
                    0
                ) AS deterioro_total

            FROM base

            GROUP BY
                id_cierre_cartera,
                fecha_corte
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return jdbc.queryForObject(
                sql,
                parametros,
                (rs, rowNum) ->
                        new ResumenAnexo1DTO(

                                rs.getObject(
                                        "id_cierre_cartera",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "fecha_corte",
                                        java.time.LocalDate.class
                                ),

                                rs.getObject(
                                        "cantidad_creditos",
                                        Integer.class
                                ),

                                rs.getBigDecimal(
                                        "saldo_capital"
                                ),

                                rs.getBigDecimal(
                                        "saldo_intereses_causados"
                                ),

                                rs.getObject(
                                        "cantidad_edad_a",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "cantidad_edad_b",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "cantidad_edad_c",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "cantidad_edad_d",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "cantidad_edad_e",
                                        Integer.class
                                ),

                                rs.getBigDecimal(
                                        "valor_aportes_aplicados"
                                ),

                                rs.getBigDecimal(
                                        "valor_garantias_asignadas"
                                ),

                                rs.getBigDecimal(
                                        "valor_garantias_reconocidas"
                                ),

                                rs.getBigDecimal(
                                        "base_deterioro_capital"
                                ),

                                rs.getBigDecimal(
                                        "deterioro_capital"
                                ),

                                rs.getBigDecimal(
                                        "deterioro_intereses"
                                ),

                                rs.getBigDecimal(
                                        "deterioro_total"
                                )
                        )
        );
    }
}
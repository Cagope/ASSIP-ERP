package co.assip.erp.cartera.cierremensual.causacionseguros;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CausacionSegurosRepository {

    private static final String OBS_CAUSACION =
            "CAUSACION SEGURO CIERRE";

    private final NamedParameterJdbcTemplate jdbc;

    public CausacionSegurosRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // VALIDAR CREDITOS CON MAS DE UN SEGURO ACTIVO
    //
    // Para el cierre debe existir como máximo una configuración
    // activa de seguro aplicable por crédito.
    // =========================================================

    public int contarCreditosConMultiplesSegurosActivos(
            Integer idCierreCartera
    ) {

        String sql = """
            WITH cierre AS
            (
                SELECT
                    fecha_corte
                FROM cartera.cierres_cartera
                WHERE id_cierre_cartera =
                      :idCierreCartera
            ),

            duplicados AS
            (
                SELECT
                    f.id_cartera_credito

                FROM cartera.cierres_cartera_creditos f

                CROSS JOIN cierre c

                INNER JOIN cartera.creditos_seguros s
                    ON s.id_cartera_credito =
                       f.id_cartera_credito

                   AND s.activo = TRUE

                   AND (
                        s.fecha_inicial_cobro IS NULL
                        OR s.fecha_inicial_cobro <=
                           c.fecha_corte
                   )

                WHERE f.id_cierre_cartera =
                      :idCierreCartera

                GROUP BY
                    f.id_cartera_credito

                HAVING COUNT(*) > 1
            )

            SELECT COUNT(*)
            FROM duplicados
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad == null
                ? 0
                : cantidad;
    }

    // =========================================================
    // VALIDAR CAUSACIONES YA CONTABILIZADAS
    //
    // Si una causación automática del mismo cierre ya tiene
    // comprobante contable, no se permite eliminarla para
    // recalcular.
    // =========================================================

    public int contarMovimientosContabilizados(
            Integer idCierreCartera
    ) {

        String sql = """
            WITH cierre AS
            (
                SELECT
                    fecha_corte
                FROM cartera.cierres_cartera
                WHERE id_cierre_cartera =
                      :idCierreCartera
            )

            SELECT COUNT(*)

            FROM cartera.creditos_seguros_detalle d

            INNER JOIN cartera.creditos_seguros s
                ON s.id_credito_seguro =
                   d.id_credito_seguro

            INNER JOIN cartera.cierres_cartera_creditos f
                ON f.id_cartera_credito =
                   s.id_cartera_credito

            CROSS JOIN cierre c

            WHERE f.id_cierre_cartera =
                  :idCierreCartera

              AND d.fecha_movimiento =
                  c.fecha_corte

              AND d.observacion_seguro =
                  :observacion

              AND
              (
                    NULLIF(
                        TRIM(d.tipo_comprobante),
                        ''
                    ) IS NOT NULL

                    OR

                    NULLIF(
                        TRIM(d.numero_comprobante),
                        ''
                    ) IS NOT NULL
              )
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "observacion",
                                OBS_CAUSACION
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad == null
                ? 0
                : cantidad;
    }

    // =========================================================
    // LIMPIAR CAUSACION AUTOMATICA NO CONTABILIZADA
    //
    // Permite volver a ejecutar el cierre mientras esté en P.
    //
    // NO elimina:
    // - pagos
    // - ajustes manuales
    // - anulaciones
    // - movimientos con comprobante
    // =========================================================

    public int limpiarCausacionNoContabilizada(
            Integer idCierreCartera
    ) {

        String sql = """
            DELETE FROM cartera.creditos_seguros_detalle d

            USING
                cartera.creditos_seguros s,
                cartera.cierres_cartera_creditos f,
                cartera.cierres_cartera c

            WHERE d.id_credito_seguro =
                  s.id_credito_seguro

              AND s.id_cartera_credito =
                  f.id_cartera_credito

              AND f.id_cierre_cartera =
                  :idCierreCartera

              AND c.id_cierre_cartera =
                  f.id_cierre_cartera

              AND d.fecha_movimiento =
                  c.fecha_corte

              AND d.observacion_seguro =
                  :observacion

              AND NULLIF(
                    TRIM(d.tipo_comprobante),
                    ''
                  ) IS NULL

              AND NULLIF(
                    TRIM(d.numero_comprobante),
                    ''
                  ) IS NULL
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "observacion",
                                OBS_CAUSACION
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CAUSAR SEGUROS
    //
    // REGLAS:
    //
    // 1. Todo cálculo parte de la FOTO DEL CIERRE.
    //
    // 2. sobre_saldo_actual:
    //      TRUE  -> saldo_actual de la foto
    //      FALSE -> valor_desembolsado de la foto
    //
    // 3. incluye_intereses_causados:
    //      TRUE  -> suma valor_intereses_causados_mes
    //      FALSE -> suma cero
    //
    // 4. base_calculo:
    //      saldo_base
    //      + intereses_causados_base
    //      + otros_base
    //
    // 5. porcentaje_aplicar:
    //      se toma de la configuración de creditos_seguros.
    //
    // 6. valor_debito:
    //      ROUND(base_calculo * porcentaje_aplicar / 100, 0)
    //
    // 7. Se deja registro incluso cuando el resultado sea cero,
    //    siempre que exista seguro activo aplicable al corte.
    // =========================================================

    public int causarSeguros(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            WITH base AS
            (
                SELECT
                    f.id_cierre_cartera_credito,
                    f.id_cartera_credito,

                    c.fecha_corte,

                    s.id_credito_seguro,

                    s.porcentaje_base_seguro,
                    s.porcentaje_extraprima,
                    s.porcentaje_aplicar,

                    s.sobre_saldo_actual,
                    s.incluye_intereses_causados,

                    CASE
                        WHEN s.sobre_saldo_actual = TRUE
                        THEN COALESCE(
                            f.saldo_actual,
                            0
                        )

                        ELSE COALESCE(
                            f.valor_desembolsado,
                            0
                        )
                    END AS saldo_base,

                    CASE
                        WHEN s.incluye_intereses_causados = TRUE
                        THEN COALESCE(
                            r.valor_intereses_causados_mes,
                            0
                        )

                        ELSE 0
                    END AS intereses_causados_base

                FROM cartera.cierres_cartera_creditos f

                INNER JOIN cartera.cierres_cartera c
                    ON c.id_cierre_cartera =
                       f.id_cierre_cartera

                INNER JOIN cartera.cierres_cartera_resultados r
                    ON r.id_cierre_cartera_credito =
                       f.id_cierre_cartera_credito

                   AND r.id_cierre_cartera =
                       f.id_cierre_cartera

                INNER JOIN cartera.creditos_seguros s
                    ON s.id_cartera_credito =
                       f.id_cartera_credito

                   AND s.activo = TRUE

                   AND
                   (
                       s.fecha_inicial_cobro IS NULL

                       OR

                       s.fecha_inicial_cobro <=
                       c.fecha_corte
                   )

                WHERE f.id_cierre_cartera =
                      :idCierreCartera
            ),

            calculo AS
            (
                SELECT
                    b.*,

                    0::numeric(18,2)
                        AS otros_base,

                    (
                        b.saldo_base
                        +
                        b.intereses_causados_base
                    )::numeric(18,2)
                        AS base_calculo

                FROM base b
            ),

            resultado AS
            (
                SELECT
                    c.*,

                    ROUND(
                        c.base_calculo
                        *
                        (
                            c.porcentaje_aplicar
                            / 100.0
                        ),
                        0
                    )::numeric(18,2)
                        AS valor_seguro

                FROM calculo c
            )

            INSERT INTO cartera.creditos_seguros_detalle
            (
                id_credito_seguro,

                fecha_movimiento,

                saldo_base,
                intereses_causados_base,
                otros_base,
                base_calculo,

                porcentaje_base_seguro,
                porcentaje_extraprima,
                porcentaje_aplicar,

                sobre_saldo_actual,
                incluye_intereses_causados,

                valor_debito,
                valor_credito,

                tipo_comprobante,
                numero_comprobante,

                observacion_seguro,
                estado,

                fk_seguridad_creacion,
                fk_seguridad_edicion
            )

            SELECT
                r.id_credito_seguro,

                r.fecha_corte,

                r.saldo_base,
                r.intereses_causados_base,
                r.otros_base,
                r.base_calculo,

                r.porcentaje_base_seguro,
                r.porcentaje_extraprima,
                r.porcentaje_aplicar,

                r.sobre_saldo_actual,
                r.incluye_intereses_causados,

                r.valor_seguro,
                0,

                NULL,
                NULL,

                :observacion,
                'A',

                :idUsuario,
                :idUsuario

            FROM resultado r
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
                        .addValue(
                                "observacion",
                                OBS_CAUSACION
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CONSOLIDAR RESULTADOS DE SEGUROS
    //
    // POR CADA CREDITO GUARDA:
    //
    // valor_seguros_mes
    //     = causación automática generada por este cierre
    //
    // saldo_seguros
    //     = débitos - créditos acumulados hasta fecha_corte
    //
    // Los pagos disminuyen saldo_seguros pero NO disminuyen
    // valor_seguros_mes.
    //
    // Una anulación de pago registrada al débito incrementa
    // nuevamente el saldo, pero tampoco se confunde con la
    // causación del mes porque no tiene OBS_CAUSACION.
    // =========================================================

    public int consolidarResultadosSeguros(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            WITH cierre AS
            (
                SELECT
                    c.id_cierre_cartera,
                    c.fecha_corte

                FROM cartera.cierres_cartera c

                WHERE c.id_cierre_cartera =
                      :idCierreCartera
            ),

            base_creditos AS
            (
                SELECT
                    f.id_cierre_cartera_credito,
                    f.id_cartera_credito

                FROM cartera.cierres_cartera_creditos f

                WHERE f.id_cierre_cartera =
                      :idCierreCartera
            ),

            seguros AS
            (
                SELECT
                    bc.id_cierre_cartera_credito,

                    -- =========================================
                    -- CAUSACION DEL CIERRE ACTUAL
                    -- =========================================

                    COALESCE(
                        SUM(
                            CASE
                                WHEN d.fecha_movimiento =
                                     c.fecha_corte

                                 AND d.observacion_seguro =
                                     :observacion

                                 AND COALESCE(
                                     d.estado,
                                     'A'
                                 ) <> 'I'

                                THEN
                                    d.valor_debito
                                    -
                                    d.valor_credito

                                ELSE 0
                            END
                        ),
                        0
                    ) AS valor_seguros_mes,

                    -- =========================================
                    -- SALDO ACUMULADO AL CORTE
                    -- =========================================

                    COALESCE(
                        SUM(
                            CASE
                                WHEN d.fecha_movimiento <=
                                     c.fecha_corte

                                 AND COALESCE(
                                     d.estado,
                                     'A'
                                 ) <> 'I'

                                THEN
                                    d.valor_debito
                                    -
                                    d.valor_credito

                                ELSE 0
                            END
                        ),
                        0
                    ) AS saldo_seguros

                FROM base_creditos bc

                CROSS JOIN cierre c

                LEFT JOIN cartera.creditos_seguros s
                    ON s.id_cartera_credito =
                       bc.id_cartera_credito

                LEFT JOIN cartera.creditos_seguros_detalle d
                    ON d.id_credito_seguro =
                       s.id_credito_seguro

                GROUP BY
                    bc.id_cierre_cartera_credito
            )

            UPDATE cartera.cierres_cartera_resultados r

               SET valor_seguros_mes =
                       GREATEST(
                           s.valor_seguros_mes,
                           0
                       ),

                   saldo_seguros =
                       GREATEST(
                           s.saldo_seguros,
                           0
                       ),

                   fecha_calculo =
                       CURRENT_TIMESTAMP,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM seguros s

             WHERE r.id_cierre_cartera_credito =
                   s.id_cierre_cartera_credito

               AND r.id_cierre_cartera =
                   :idCierreCartera
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
                        .addValue(
                                "observacion",
                                OBS_CAUSACION
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }
}
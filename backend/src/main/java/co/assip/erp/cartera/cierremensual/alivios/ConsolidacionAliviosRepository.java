package co.assip.erp.cartera.cierremensual.alivios;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConsolidacionAliviosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ConsolidacionAliviosRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // CONSOLIDAR ALIVIOS DEL CIERRE
    //
    // valor_alivios_mes
    //     = movimiento neto del mes
    //
    // saldo_alivios
    //     = débitos - créditos acumulados hasta fecha_corte
    //
    // No genera movimientos nuevos.
    // Solo consolida el histórico existente.
    // =========================================================

    public int consolidarResultadosAlivios(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            WITH cierre AS
            (
                SELECT
                    c.id_cierre_cartera,
                    c.fecha_corte,

                    date_trunc(
                        'month',
                        c.fecha_corte
                    )::date AS fecha_inicio_mes

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

            alivios AS
            (
                SELECT
                    bc.id_cierre_cartera_credito,

                    -- =========================================
                    -- MOVIMIENTO NETO DEL MES
                    -- =========================================

                    COALESCE(
                        SUM(
                            CASE
                                WHEN d.fecha_movimiento
                                     BETWEEN c.fecha_inicio_mes
                                         AND c.fecha_corte

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
                    ) AS valor_alivios_mes,

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
                    ) AS saldo_alivios

                FROM base_creditos bc

                CROSS JOIN cierre c

                LEFT JOIN cartera.creditos_alivios a
                    ON a.id_cartera_credito =
                       bc.id_cartera_credito

                LEFT JOIN cartera.creditos_alivios_detalle d
                    ON d.id_credito_alivio =
                       a.id_credito_alivio

                GROUP BY
                    bc.id_cierre_cartera_credito
            )

            UPDATE cartera.cierres_cartera_resultados r

               SET valor_alivios_mes =
                       GREATEST(
                           a.valor_alivios_mes,
                           0
                       ),

                   saldo_alivios =
                       GREATEST(
                           a.saldo_alivios,
                           0
                       ),

                   fecha_calculo =
                       CURRENT_TIMESTAMP,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM alivios a

             WHERE r.id_cierre_cartera_credito =
                   a.id_cierre_cartera_credito

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
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }
}
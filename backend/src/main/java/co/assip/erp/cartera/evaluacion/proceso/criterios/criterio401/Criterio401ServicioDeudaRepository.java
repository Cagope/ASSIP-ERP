package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio401;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio401.dto.Criterio401DatoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Criterio401ServicioDeudaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // OBTENER DATOS DEL CRITERIO 401
    //
    // Evalúa los pagos registrados durante el último año,
    // contado desde la fecha de corte.
    //
    // Cada pago se consolida por:
    //
    // id_cartera_credito
    // + tipo_comprobante
    // + numero_comprobante
    //
    // Esto evita contar varias veces las filas contables
    // generadas por un mismo comprobante.
    // =========================================================

    public List<Criterio401DatoDTO> obtenerDatos(
            LocalDate fechaCorte
    ) {

        LocalDate fechaInicial =
                fechaCorte
                        .minusYears(1)
                        .plusDays(1);

        String sql = """
                WITH pagos_consolidados AS
                (
                    SELECT
                        ec.id_cartera_credito,

                        ec.tipo_comprobante,

                        ec.numero_comprobante,

                        SUM(
                            COALESCE(
                                ec.valor_capital,
                                0
                            )
                        )
                            AS valor_capital,

                        /*
                         * Un mismo comprobante puede generar varias
                         * filas contables.
                         *
                         * Se toma la mayor cantidad de días de mora
                         * informada dentro del comprobante.
                         */
                        MAX(ec.dias_mora)
                            AS dias_mora

                    FROM cartera.extractos_cartera ec

                    WHERE ec.fecha_contable
                          BETWEEN :fechaInicial
                              AND :fechaCorte

                    GROUP BY
                        ec.id_cartera_credito,
                        ec.tipo_comprobante,
                        ec.numero_comprobante
                ),

                servicio_deuda AS
                (
                    SELECT
                        pc.id_cartera_credito,

                        /*
                         * Cada registro consolidado representa
                         * un único pago o comprobante.
                         */
                        COUNT(*)
                            AS cantidad_pagos,

                        /*
                         * Campo informativo:
                         * pagos que registraron abono a capital.
                         */
                        COUNT(*) FILTER
                        (
                            WHERE pc.valor_capital > 0
                        )
                            AS cantidad_pagos_capital,

                        /*
                         * Participan en el promedio los pagos que
                         * tienen informado el campo dias_mora.
                         */
                        COUNT(*) FILTER
                        (
                            WHERE pc.dias_mora IS NOT NULL
                        )
                            AS cantidad_pagos_evaluables,

                        COALESCE(
                            SUM(pc.dias_mora) FILTER
                            (
                                WHERE pc.dias_mora IS NOT NULL
                            ),
                            0
                        )
                            AS suma_dias_mora,

                        /*
                         * El promedio se redondea siempre hacia arriba
                         * para evitar valores decimales entre rangos.
                         */
                        CEIL(
                            AVG(
                                pc.dias_mora::numeric
                            ) FILTER
                            (
                                WHERE pc.dias_mora IS NOT NULL
                            )
                        )
                            AS promedio_dias_mora

                    FROM pagos_consolidados pc

                    GROUP BY
                        pc.id_cartera_credito
                )

                SELECT
                    cc.id_cierre_cartera_credito
                        AS idCierreCarteraCredito,

                    cc.id_cartera_credito
                        AS idCarteraCredito,

                    cc.id_datos_personal
                        AS idDatosPersonal,

                    hv.documento,

                    COALESCE(
                        sd.cantidad_pagos,
                        0
                    )
                        AS cantidadPagosUltimoAnio,

                    COALESCE(
                        sd.cantidad_pagos_capital,
                        0
                    )
                        AS cantidadPagosCapitalUltimoAnio,

                    COALESCE(
                        sd.cantidad_pagos_evaluables,
                        0
                    )
                        AS cantidadPagosEvaluablesUltimoAnio,

                    COALESCE(
                        sd.suma_dias_mora,
                        0
                    )
                        AS sumaDiasMoraUltimoAnio,

                    sd.promedio_dias_mora
                        AS promedioDiasMoraUltimoAnio

                FROM cartera.cierres_cartera cierre

                INNER JOIN cartera.cierres_cartera_creditos cc
                    ON cc.id_cierre_cartera =
                       cierre.id_cierre_cartera

                INNER JOIN hoja_vida.cierres_hoja_vida cierre_hv
                    ON cierre_hv.fecha_corte =
                       cierre.fecha_corte

                   AND cierre_hv.estado = 'D'

                INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                    ON hv.id_cierre_hoja_vida =
                       cierre_hv.id_cierre_hoja_vida

                   AND hv.id_datos_personal =
                       cc.id_datos_personal

                LEFT JOIN servicio_deuda sd
                    ON sd.id_cartera_credito =
                       cc.id_cartera_credito

                WHERE cierre.fecha_corte =
                      :fechaCorte

                ORDER BY
                    cierre.id_agencia,
                    hv.documento,
                    cc.id_cartera_credito
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaInicial",
                                fechaInicial
                        )
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        Criterio401DatoDTO.class
                )
        );
    }
}
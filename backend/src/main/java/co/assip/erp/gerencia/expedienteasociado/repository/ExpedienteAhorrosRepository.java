package co.assip.erp.gerencia.expedienteasociado.repository;

import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteCuentaAhorroDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExpedienteAhorrosRepository
        extends ExpedienteRepositorySupport {

    // =========================================================
    // SQL: cuentas de ahorro
    // =========================================================

    private static final String SQL_CUENTAS_AHORRO = """
            SELECT
                a.id_cuenta_ahorro,
                a.id_datos_personal,
                a.documento,
                a.nombre_completo,

                a.id_forma_ahorro,
                a.codigo_forma_ahorro,
                a.nombre_forma_ahorro,

                a.codigo_tipo_captacion,
                a.nombre_tipo_captacion,

                a.codigo_cuenta
                    AS numero_cuenta,

                NULL::varchar
                    AS numero_libreta,

                a.codigo_estado_cuenta,
                a.nombre_estado_cuenta,

                COALESCE(
                    a.estado_operativo,
                    false
                ) AS activa,

                false
                    AS bloqueada,

                false
                    AS embargada,

                CASE
                    WHEN upper(
                        trim(
                            COALESCE(
                                a.nombre_estado_cuenta,
                                ''
                            )
                        )
                    ) IN (
                        'CANCELADA',
                        'CANCELADO'
                    )
                        THEN true
                    ELSE false
                END AS cancelada,

                NOT COALESCE(
                    a.estado_operativo,
                    false
                ) AS cuenta_inactiva,

                a.fecha_apertura_cuenta
                    AS fecha_apertura,

                mov.fecha_ultimo_movimiento,

                mov.dias_sin_movimiento,

                COALESCE(
                    a.saldo_actual_cuenta,
                    0
                )::numeric(18,2)
                    AS saldo_disponible,

                0::numeric(18,2)
                    AS saldo_canje,

                COALESCE(
                    a.saldo_actual_cuenta,
                    0
                )::numeric(18,2)
                    AS saldo_total,

                NULL::numeric
                    AS saldo_promedio,

                COALESCE(
                    mov.total_consignaciones,
                    0
                )::numeric(18,2)
                    AS total_consignaciones,

                COALESCE(
                    mov.total_retiros,
                    0
                )::numeric(18,2)
                    AS total_retiros,

                0::numeric(18,2)
                    AS total_intereses,

                COALESCE(
                    mov.cantidad_entradas_mes,
                    0
                )::integer
                    AS cantidad_entradas_mes,

                COALESCE(
                    mov.valor_entradas_mes,
                    0
                )::numeric(18,2)
                    AS valor_entradas_mes,

                COALESCE(
                    mov.cantidad_salidas_mes,
                    0
                )::integer
                    AS cantidad_salidas_mes,

                COALESCE(
                    mov.valor_salidas_mes,
                    0
                )::numeric(18,2)
                    AS valor_salidas_mes,

                (
                    COALESCE(
                        mov.cantidad_entradas_mes,
                        0
                    )
                    +
                    COALESCE(
                        mov.cantidad_salidas_mes,
                        0
                    )
                )::integer
                    AS cantidad_movimientos_mes,

                (
                    COALESCE(
                        mov.valor_entradas_mes,
                        0
                    )
                    +
                    COALESCE(
                        mov.valor_salidas_mes,
                        0
                    )
                )::numeric(18,2)
                    AS valor_movimientos_mes,

                COALESCE(
                    mov.cantidad_movimientos_ano,
                    0
                )::integer
                    AS cantidad_movimientos_ano,

                COALESCE(
                    mov.valor_movimientos_ano,
                    0
                )::numeric(18,2)
                    AS valor_movimientos_ano,

                false
                    AS genera_intereses,

                false
                    AS exenta_gmf,

                upper(
                    trim(
                        COALESCE(
                            a.codigo_cuenta_conjunta,
                            ''
                        )
                    )
                ) = 'S'
                    AS cuenta_conjunta,

                false
                    AS tiene_beneficiarios,

                false
                    AS tiene_apoderados,

                0::integer
                    AS cantidad_cheques_canje,

                0::numeric(18,2)
                    AS valor_cheques_canje,

                COALESCE(
                    a.saldo_actual_cuenta,
                    0
                ) < 0
                    AS saldo_negativo,

                false
                    AS movimientos_inusuales,

                (
                    COALESCE(
                        a.saldo_actual_cuenta,
                        0
                    ) < 0

                    OR (
                        NOT COALESCE(
                            a.estado_operativo,
                            false
                        )

                        AND COALESCE(
                            a.saldo_actual_cuenta,
                            0
                        ) <> 0
                    )
                ) AS requiere_revision,

                CASE
                    WHEN COALESCE(
                        a.saldo_actual_cuenta,
                        0
                    ) < 0
                        THEN 'CRITICA'

                    WHEN (
                        NOT COALESCE(
                            a.estado_operativo,
                            false
                        )

                        AND COALESCE(
                            a.saldo_actual_cuenta,
                            0
                        ) <> 0
                    )
                        THEN 'ADVERTENCIA'

                    ELSE 'NORMAL'
                END AS nivel_alerta

            FROM reporting.vw_depositos_cuentas_ahorro_integral a

            LEFT JOIN LATERAL (
                SELECT
                    MAX(m.fecha_movimiento)::date
                        AS fecha_ultimo_movimiento,

                    CASE
                        WHEN MAX(m.fecha_movimiento) IS NULL
                            THEN NULL

                        ELSE GREATEST(
                            current_date
                                - MAX(m.fecha_movimiento)::date,
                            0
                        )::integer
                    END AS dias_sin_movimiento,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN COALESCE(
                                    m.valor_credito,
                                    0
                                ) > 0
                                    THEN m.valor_credito
                                ELSE 0
                            END
                        ),
                        0
                    )::numeric(18,2)
                        AS total_consignaciones,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN COALESCE(
                                    m.valor_debito,
                                    0
                                ) > 0
                                    THEN m.valor_debito
                                ELSE 0
                            END
                        ),
                        0
                    )::numeric(18,2)
                        AS total_retiros,

                    COUNT(*) FILTER (
                        WHERE m.fecha_movimiento >=
                              date_trunc(
                                  'month',
                                  current_date
                              )::date

                          AND m.fecha_movimiento <
                              (
                                  date_trunc(
                                      'month',
                                      current_date
                                  )
                                  + interval '1 month'
                              )::date

                          AND COALESCE(
                              m.valor_credito,
                              0
                          ) > 0
                    )::integer
                        AS cantidad_entradas_mes,

                    COALESCE(
                        SUM(m.valor_credito) FILTER (
                            WHERE m.fecha_movimiento >=
                                  date_trunc(
                                      'month',
                                      current_date
                                  )::date

                              AND m.fecha_movimiento <
                                  (
                                      date_trunc(
                                          'month',
                                          current_date
                                      )
                                      + interval '1 month'
                                  )::date

                              AND COALESCE(
                                  m.valor_credito,
                                  0
                              ) > 0
                        ),
                        0
                    )::numeric(18,2)
                        AS valor_entradas_mes,

                    COUNT(*) FILTER (
                        WHERE m.fecha_movimiento >=
                              date_trunc(
                                  'month',
                                  current_date
                              )::date

                          AND m.fecha_movimiento <
                              (
                                  date_trunc(
                                      'month',
                                      current_date
                                  )
                                  + interval '1 month'
                              )::date

                          AND COALESCE(
                              m.valor_debito,
                              0
                          ) > 0
                    )::integer
                        AS cantidad_salidas_mes,

                    COALESCE(
                        SUM(m.valor_debito) FILTER (
                            WHERE m.fecha_movimiento >=
                                  date_trunc(
                                      'month',
                                      current_date
                                  )::date

                              AND m.fecha_movimiento <
                                  (
                                      date_trunc(
                                          'month',
                                          current_date
                                      )
                                      + interval '1 month'
                                  )::date

                              AND COALESCE(
                                  m.valor_debito,
                                  0
                              ) > 0
                        ),
                        0
                    )::numeric(18,2)
                        AS valor_salidas_mes,

                    (
                        COUNT(*) FILTER (
                            WHERE m.fecha_movimiento >=
                                  date_trunc(
                                      'year',
                                      current_date
                                  )::date

                              AND m.fecha_movimiento <
                                  (
                                      date_trunc(
                                          'year',
                                          current_date
                                      )
                                      + interval '1 year'
                                  )::date

                              AND COALESCE(
                                  m.valor_credito,
                                  0
                              ) > 0
                        )
                        +
                        COUNT(*) FILTER (
                            WHERE m.fecha_movimiento >=
                                  date_trunc(
                                      'year',
                                      current_date
                                  )::date

                              AND m.fecha_movimiento <
                                  (
                                      date_trunc(
                                          'year',
                                          current_date
                                      )
                                      + interval '1 year'
                                  )::date

                              AND COALESCE(
                                  m.valor_debito,
                                  0
                              ) > 0
                        )
                    )::integer
                        AS cantidad_movimientos_ano,

                    (
                        COALESCE(
                            SUM(m.valor_credito) FILTER (
                                WHERE m.fecha_movimiento >=
                                      date_trunc(
                                          'year',
                                          current_date
                                      )::date

                                  AND m.fecha_movimiento <
                                      (
                                          date_trunc(
                                              'year',
                                              current_date
                                          )
                                          + interval '1 year'
                                      )::date

                                  AND COALESCE(
                                      m.valor_credito,
                                      0
                                  ) > 0
                            ),
                            0
                        )
                        +
                        COALESCE(
                            SUM(m.valor_debito) FILTER (
                                WHERE m.fecha_movimiento >=
                                      date_trunc(
                                          'year',
                                          current_date
                                      )::date

                                  AND m.fecha_movimiento <
                                      (
                                          date_trunc(
                                              'year',
                                              current_date
                                          )
                                          + interval '1 year'
                                      )::date

                                  AND COALESCE(
                                      m.valor_debito,
                                      0
                                  ) > 0
                            ),
                            0
                        )
                    )::numeric(18,2)
                        AS valor_movimientos_ano

                FROM depositos.vw_depositos_extractos_cuentas_detalle m

                WHERE m.id_cuenta_ahorro =
                      a.id_cuenta_ahorro
            ) mov
                ON true

            WHERE a.id_datos_personal = :idDatosPersonal

            ORDER BY
                CASE
                    WHEN COALESCE(
                        a.estado_operativo,
                        false
                    )
                        THEN 0
                    ELSE 1
                END,

                a.fecha_apertura_cuenta DESC NULLS LAST,
                a.id_cuenta_ahorro DESC
            """;

    // =========================================================
    // Constructor
    // =========================================================

    public ExpedienteAhorrosRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        super(jdbc);
    }

    // =========================================================
    // Consulta
    // =========================================================

    public List<ExpedienteCuentaAhorroDTO> listarCuentasAhorro(
            Long idDatosPersonal
    ) {

        validarIdDatosPersonal(idDatosPersonal);

        List<ExpedienteCuentaAhorroDTO> cuentas =
                jdbc.query(
                        SQL_CUENTAS_AHORRO,
                        parametros(idDatosPersonal),
                        mapper(ExpedienteCuentaAhorroDTO.class)
                );

        cuentas.forEach(
                ExpedienteCuentaAhorroDTO::evaluarEstadoCuenta
        );

        return cuentas;
    }
}
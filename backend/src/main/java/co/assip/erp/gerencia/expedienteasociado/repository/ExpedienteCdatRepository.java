package co.assip.erp.gerencia.expedienteasociado.repository;

import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteCdatDTO;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExpedienteCdatRepository
        extends ExpedienteRepositorySupport {

    // =========================================================
    // SQL: CDAT
    // =========================================================

    private static final String SQL_CDATS = """
            SELECT
                c.id_cuenta_cdat
                    AS id_cdat,

                c.id_datos_personal,

                c.documento,

                c.nombre_completo_apellidos
                    AS nombre_completo,

                c.codigo_cdat
                    AS numero_cdat,

                c.estado_cdat
                    AS codigo_estado,

                c.descripcion_estado_cdat
                    AS nombre_estado,

                -- =================================================
                -- Estado operativo
                -- =================================================

                (
                    c.fecha_cancelacion_cdat IS NULL
                    AND COALESCE(c.saldo_actual_cdat, 0) > 0
                ) AS activo,

                (
                    c.fecha_cancelacion_cdat IS NOT NULL
                    OR COALESCE(c.saldo_actual_cdat, 0) <= 0
                ) AS cancelado,

                (
                    c.fecha_cancelacion_cdat IS NULL
                    AND COALESCE(c.saldo_actual_cdat, 0) > 0
                    AND c.fecha_vencimiento_cdat < current_date
                ) AS vencido,

                (
                    c.fecha_cancelacion_cdat IS NULL
                    AND COALESCE(c.saldo_actual_cdat, 0) > 0
                    AND c.fecha_vencimiento_cdat
                        BETWEEN current_date
                            AND current_date + 30
                ) AS proximo_vencer,

                -- =================================================
                -- Fechas
                -- =================================================

                c.fecha_apertura_cdat
                    AS fecha_constitucion,

                c.fecha_vencimiento_cdat
                    AS fecha_vencimiento,

                c.fecha_cancelacion_cdat
                    AS fecha_cancelacion,

                mov.fecha_ultimo_movimiento,

                mov.fecha_ultimo_pago_intereses,

                c.plazo_dias,

                CASE
                    WHEN c.fecha_apertura_cdat IS NULL
                        THEN NULL

                    ELSE GREATEST(
                        current_date - c.fecha_apertura_cdat,
                        0
                    )::integer
                END AS dias_transcurridos,

                CASE
                    WHEN c.fecha_vencimiento_cdat IS NULL
                        THEN NULL

                    ELSE (
                        c.fecha_vencimiento_cdat - current_date
                    )::integer
                END AS dias_para_vencimiento,

                -- =================================================
                -- Capital
                -- =================================================

                COALESCE(
                    c.valor_apertura_cdat,
                    0
                )::numeric(18,2)
                    AS capital_inicial,

                COALESCE(
                    NULLIF(mov.capital_historico_invertido, 0),
                    c.valor_apertura_cdat,
                    0
                )::numeric(18,2)
                    AS capital_historico_invertido,

                CASE
                    WHEN c.fecha_cancelacion_cdat IS NULL
                     AND COALESCE(c.saldo_actual_cdat, 0) > 0
                        THEN COALESCE(c.saldo_actual_cdat, 0)

                    ELSE 0
                END::numeric(18,2)
                    AS capital_vigente,

                COALESCE(
                    c.saldo_actual_cdat,
                    0
                )::numeric(18,2)
                    AS saldo_actual,

                COALESCE(
                    c.saldo_actual_cdat,
                    0
                )::numeric(18,2)
                    AS saldo_total,

                -- =================================================
                -- Rendimiento histórico
                -- =================================================

                COALESCE(
                    mov.intereses_liquidados,
                    0
                )::numeric(18,2)
                    AS intereses_liquidados,

                COALESCE(
                    mov.retencion_fuente,
                    0
                )::numeric(18,2)
                    AS retencion_fuente,

                (
                    COALESCE(
                        mov.intereses_liquidados,
                        0
                    )
                    -
                    COALESCE(
                        mov.retencion_fuente,
                        0
                    )
                )::numeric(18,2)
                    AS rendimiento_historico,

                COALESCE(
                    mov.cantidad_pagos_intereses,
                    0
                )::integer
                    AS cantidad_pagos_intereses,

                COALESCE(
                    mov.cantidad_movimientos,
                    0
                )::integer
                    AS cantidad_movimientos,

                -- =================================================
                -- Tasas
                -- =================================================

                c.tasa_efectiva_anual
                    AS tasa_ea,

                c.tasa_nominal_anual
                    AS tasa_nominal,

                -- =================================================
                -- Titularidad
                -- =================================================

                (
                    upper(
                        trim(
                            COALESCE(
                                c.cuenta_conjunta,
                                ''
                            )
                        )
                    ) = 'S'
                ) AS conjunto,

                CASE
                    WHEN upper(
                        trim(
                            COALESCE(
                                c.cuenta_conjunta,
                                ''
                            )
                        )
                    ) = 'S'
                        THEN 2

                    ELSE 1
                END AS numero_titulares,

                -- =================================================
                -- Alertas
                -- =================================================

                CASE
                    WHEN (
                        c.fecha_cancelacion_cdat IS NULL
                        AND COALESCE(c.saldo_actual_cdat, 0) > 0
                        AND c.fecha_vencimiento_cdat < current_date
                    )
                        THEN 'CRITICA'

                    WHEN (
                        c.fecha_cancelacion_cdat IS NULL
                        AND COALESCE(c.saldo_actual_cdat, 0) > 0
                        AND c.fecha_vencimiento_cdat
                            BETWEEN current_date
                                AND current_date + 30
                    )
                        THEN 'ADVERTENCIA'

                    ELSE 'NORMAL'
                END AS nivel_alerta,

                c.observacion
                    AS observaciones

            FROM cdat.vw_cdat_cuentas_total_extendida c

                        -- =====================================================
            -- Extracto histórico del CDAT
            -- =====================================================

            LEFT JOIN LATERAL (
                SELECT
                    MAX(e.fecha_movimiento)
                        AS fecha_ultimo_movimiento,

                    MAX(e.fecha_movimiento) FILTER (
                        WHERE trim(e.tipo_movimiento) = '051'
                    ) AS fecha_ultimo_pago_intereses,

                    -- ---------------------------------------------
                    -- 001: apertura CDAT
                    -- ---------------------------------------------

                    COALESCE(
                        SUM(
                            CASE
                                WHEN trim(e.tipo_movimiento) = '001'
                                    THEN GREATEST(
                                        ABS(COALESCE(e.valor_debito, 0)),
                                        ABS(COALESCE(e.valor_credito, 0))
                                    )

                                ELSE 0
                            END
                        ),
                        0
                    )::numeric(18,2)
                        AS capital_historico_invertido,

                    -- ---------------------------------------------
                    -- 005: intereses liquidados
                    -- ---------------------------------------------

                    COALESCE(
                        SUM(
                            CASE
                                WHEN trim(e.tipo_movimiento) = '005'
                                    THEN GREATEST(
                                        ABS(COALESCE(e.valor_debito, 0)),
                                        ABS(COALESCE(e.valor_credito, 0))
                                    )

                                ELSE 0
                            END
                        ),
                        0
                    )::numeric(18,2)
                        AS intereses_liquidados,

                    -- ---------------------------------------------
                    -- 051: pago neto de intereses
                    -- ---------------------------------------------

                    COALESCE(
                        SUM(
                            CASE
                                WHEN trim(e.tipo_movimiento) = '051'
                                    THEN GREATEST(
                                        ABS(COALESCE(e.valor_debito, 0)),
                                        ABS(COALESCE(e.valor_credito, 0))
                                    )

                                ELSE 0
                            END
                        ),
                        0
                    )::numeric(18,2)
                        AS pagos_intereses,

                    -- ---------------------------------------------
                    -- 056: retención en la fuente
                    -- ---------------------------------------------

                    COALESCE(
                        SUM(
                            CASE
                                WHEN trim(e.tipo_movimiento) = '056'
                                    THEN GREATEST(
                                        ABS(COALESCE(e.valor_debito, 0)),
                                        ABS(COALESCE(e.valor_credito, 0))
                                    )

                                ELSE 0
                            END
                        ),
                        0
                    )::numeric(18,2)
                        AS retencion_fuente,

                    COUNT(*) FILTER (
                        WHERE trim(e.tipo_movimiento) = '051'
                    )::integer
                        AS cantidad_pagos_intereses,

                    COUNT(*)::integer
                        AS cantidad_movimientos

                FROM cdat.vw_cdat_extractos_total e

                WHERE e.id_cuenta_cdat =
                      c.id_cuenta_cdat
            ) mov
                ON true

            WHERE c.id_datos_personal = :idDatosPersonal

            ORDER BY
                CASE
                    WHEN c.fecha_cancelacion_cdat IS NULL
                     AND COALESCE(c.saldo_actual_cdat, 0) > 0
                        THEN 0

                    ELSE 1
                END,

                c.fecha_vencimiento_cdat ASC NULLS LAST,

                c.fecha_apertura_cdat DESC NULLS LAST,

                c.id_cuenta_cdat DESC
            """;

    // =========================================================
    // Constructor
    // =========================================================

    public ExpedienteCdatRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        super(jdbc);
    }

    // =========================================================
    // Consulta
    // =========================================================

    public List<ExpedienteCdatDTO> listarCdats(
            Long idDatosPersonal
    ) {

        validarIdDatosPersonal(idDatosPersonal);

        List<ExpedienteCdatDTO> cdats =
                jdbc.query(
                        SQL_CDATS,
                        parametros(idDatosPersonal),
                        mapper(ExpedienteCdatDTO.class)
                );

        cdats.forEach(
                ExpedienteCdatDTO::evaluarEstado
        );

        return cdats;
    }
}
package co.assip.erp.depositos.informes.gmf_semanal;

import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalItemDTO;
import co.assip.erp.depositos.informes.gmf_semanal.dto.GmfSemanalRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GmfSemanalRepository {

    private final JdbcTemplate jdbc;

    public List<GmfSemanalItemDTO> consultar(
            GmfSemanalRequestDTO request
    ) {

        Integer idAgencia =
                request.getIdAgencia() == null
                        ? 0
                        : request.getIdAgencia();

        String codigoForma =
                request.getCodigoForma() == null
                        || request.getCodigoForma().isBlank()
                        ? "0"
                        : request.getCodigoForma().trim();

        String sql = """
            WITH hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    documento,
                    tipo_persona,
                    nombres,
                    primer_apellido,
                    segundo_apellido
                FROM reporting.vw_hoja_vida_general_total_reciente
                ORDER BY id_datos_personal
            ),

            movimientos AS (
                SELECT
                    e.id_extracto_cuenta_ahorro,
                    e.id_cuenta_ahorro,
                    e.fecha_movimiento,
                    e.hora_movimiento,
                    TRIM(e.tipo_movimiento) AS tipo_movimiento,
                    e.tipo_comprobante,
                    e.numero_comprobante,

                    COALESCE(e.valor_debito, 0) AS valor_debito,
                    COALESCE(e.valor_credito, 0) AS valor_credito,

                    ca.id_agencia,
                    ca.codigo_cuenta,
                    ca.id_datos_personal,
                    TRIM(COALESCE(ca.gmf_cuenta_cuenta, 'N'))
                        AS estado_gmf_cuenta,

                    fa.codigo_forma,
                    fa.nombre_forma,
                    fa.tipo_captacion_forma,

                    tm.descripcion AS descripcion_movimiento,
                    TRIM(COALESCE(tm.accion_movimiento, ''))
                        AS accion_movimiento,

                    hv.documento AS documento_asociado,

                    CASE
                        WHEN hv.tipo_persona = '2'
                            THEN COALESCE(hv.nombres, '')
                        ELSE TRIM(
                            CONCAT(
                                COALESCE(hv.primer_apellido, ''),
                                ' ',
                                COALESCE(hv.segundo_apellido, ''),
                                ' ',
                                COALESCE(hv.nombres, '')
                            )
                        )
                    END AS nombre_asociado

                FROM depositos.extractos_cuentas_ahorros e

                INNER JOIN depositos.cuentas_ahorro ca
                    ON ca.id_cuenta_ahorro =
                       e.id_cuenta_ahorro

                INNER JOIN depositos.formas_ahorro fa
                    ON fa.id_forma_ahorro =
                       ca.id_forma_ahorro

                INNER JOIN depositos.tipo_movimiento tm
                    ON TRIM(tm.codigo_movimiento) =
                       TRIM(e.tipo_movimiento)

                LEFT JOIN hv_unica hv
                    ON hv.id_datos_personal =
                       ca.id_datos_personal

                WHERE e.fecha_movimiento >= ?::date
                  AND e.fecha_movimiento <= ?::date

                  -- Aportes sociales no participan en GMF
                  AND TRIM(
                        COALESCE(
                            fa.tipo_captacion_forma,
                            ''
                        )
                      ) <> '1'

                  AND (
                        ? = 0
                        OR ca.id_agencia = ?
                      )

                  AND (
                        ? = '0'
                        OR TRIM(fa.codigo_forma) = ?
                      )
            ),

            con_parametro AS (
                SELECT
                    m.*,

                    COALESCE(pg.porcentaje_gmf, 0)
                        AS porcentaje_gmf,

                    COALESCE(pg.valor_tope_exencion, 0)
                        AS valor_tope_exencion

                FROM movimientos m

                LEFT JOIN LATERAL (
                    SELECT
                        p.porcentaje_gmf,
                        p.valor_tope_exencion
                    FROM depositos.parametros_gmf p
                    WHERE p.fecha_inicial <=
                          m.fecha_movimiento

                      AND (
                            p.fecha_final IS NULL
                            OR p.fecha_final >=
                               m.fecha_movimiento
                          )

                      AND COALESCE(p.activo, false) = true

                    ORDER BY
                        p.fecha_inicial DESC,
                        p.id_parametro_gmf DESC

                    LIMIT 1
                ) pg ON true
            ),

            con_acumulado AS (
                SELECT
                    m.*,

                    COALESCE(
                        (
                            SELECT
                                SUM(
                                    COALESCE(
                                        e2.valor_debito,
                                        0
                                    )
                                )

                            FROM
                                depositos.extractos_cuentas_ahorros e2

                            INNER JOIN
                                depositos.tipo_movimiento tm2
                                ON TRIM(
                                       tm2.codigo_movimiento
                                   )
                                   =
                                   TRIM(
                                       e2.tipo_movimiento
                                   )

                            WHERE
                                e2.id_cuenta_ahorro =
                                m.id_cuenta_ahorro

                                AND
                                e2.fecha_movimiento >=
                                DATE_TRUNC(
                                    'month',
                                    m.fecha_movimiento
                                )::date

                                AND
                                e2.fecha_movimiento <=
                                m.fecha_movimiento

                                AND TRIM(
                                        tm2.codigo_movimiento
                                    ) IN (
                                        '551',
                                        '881',
                                        '778',
                                        '885'
                                    )

                                -- Hasta el movimiento que
                                -- estamos evaluando
                                AND (
                                    e2.fecha_movimiento <
                                    m.fecha_movimiento

                                    OR (

                                        e2.fecha_movimiento =
                                        m.fecha_movimiento

                                        AND (

                                            COALESCE(
                                                e2.hora_movimiento,
                                                TIME '00:00:00'
                                            )
                                            <
                                            COALESCE(
                                                m.hora_movimiento,
                                                TIME '00:00:00'
                                            )

                                            OR (

                                                COALESCE(
                                                    e2.hora_movimiento,
                                                    TIME '00:00:00'
                                                )
                                                =
                                                COALESCE(
                                                    m.hora_movimiento,
                                                    TIME '00:00:00'
                                                )

                                                AND
                                                e2.id_extracto_cuenta_ahorro
                                                <=
                                                m.id_extracto_cuenta_ahorro
                                            )
                                        )
                                    )
                                )
                        ),
                        0
                    ) AS acumulado_mes

                FROM con_parametro m
            ),

            calculado AS (
                SELECT
                    m.*,

                    /*
                     * Acumulado antes del movimiento actual.
                     *
                     * Solamente se resta el movimiento actual
                     * cuando realmente es un retiro.
                     */
                    CASE
                        WHEN m.accion_movimiento = 'R'
                        THEN GREATEST(
                            m.acumulado_mes -
                            m.valor_debito,
                            0
                        )
                        ELSE m.acumulado_mes
                    END AS acumulado_anterior

                FROM con_acumulado m
            ),

            bases AS (
                SELECT
                    m.*,

                    /*
                     * =================================================
                     * VALOR GRAVADO PARA CUENTAS U / N
                     * =================================================
                     *
                     * VB:
                     * - controla desde el primero del mes;
                     * - antes del tope queda exento;
                     * - cuando cruza el tope solo grava el exceso;
                     * - superado el tope, grava todo.
                     */
                    CASE
                        WHEN m.accion_movimiento <> 'R'
                            THEN 0

                        WHEN m.estado_gmf_cuenta = 'S'
                            THEN m.valor_debito

                        WHEN m.estado_gmf_cuenta IN ('U', 'N')
                            THEN GREATEST(
                                m.valor_debito
                                -
                                GREATEST(
                                    m.valor_tope_exencion
                                    -
                                    m.acumulado_anterior,
                                    0
                                ),
                                0
                            )

                        ELSE 0
                    END AS valor_gravado_retiro,

                    CASE
                        WHEN m.accion_movimiento <> 'R'
                            THEN 0

                        WHEN m.estado_gmf_cuenta = 'S'
                            THEN 0

                        WHEN m.estado_gmf_cuenta IN ('U', 'N')
                            THEN LEAST(
                                m.valor_debito,
                                GREATEST(
                                    m.valor_tope_exencion
                                    -
                                    m.acumulado_anterior,
                                    0
                                )
                            )

                        ELSE 0
                    END AS valor_exento_retiro

                FROM calculado m
            )

            SELECT
                fecha_movimiento,

                TRIM(codigo_cuenta)
                    AS codigo_cuenta,

                tipo_movimiento,

                descripcion_movimiento,

                TRIM(
                    CONCAT(
                        COALESCE(tipo_comprobante, ''),
                        CASE
                            WHEN tipo_comprobante IS NOT NULL
                                 AND numero_comprobante IS NOT NULL
                                THEN '-'
                            ELSE ''
                        END,
                        COALESCE(numero_comprobante, '')
                    )
                ) AS documento_soporte,

                documento_asociado,

                nombre_asociado,

                TRIM(codigo_forma)
                    AS codigo_forma,

                nombre_forma,

                estado_gmf_cuenta,

                /*
                 * =============================================
                 * BASE ASUMIDA
                 * VB: 23 / 15
                 * ERP: 223 / 115
                 * =============================================
                 */
                CASE
                    WHEN tipo_movimiento IN ('223', '115')
                    THEN valor_credito
                    ELSE 0
                END AS base_asumido,

                CASE
                    WHEN tipo_movimiento IN ('223', '115')
                    THEN ROUND(
                        valor_credito
                        *
                        (
                            porcentaje_gmf
                            / 1000
                        ),
                        0
                    )
                    ELSE 0
                END AS gmf_asumido,

                /*
                 * =============================================
                 * BASE ASOCIADO
                 * VB:
                 * 71,72,73,80,74,55,87
                 *
                 * ERP:
                 * 771,772,773,880,774,555,887
                 * =============================================
                 */
                CASE
                    WHEN tipo_movimiento IN (
                        '771',
                        '772',
                        '773',
                        '880',
                        '774',
                        '555',
                        '887'
                    )
                    THEN valor_debito
                    ELSE 0
                END AS base_asociado,

                CASE
                    WHEN tipo_movimiento IN (
                        '771',
                        '772',
                        '773',
                        '880',
                        '774',
                        '555',
                        '887'
                    )
                    THEN ROUND(
                        valor_debito
                        *
                        (
                            porcentaje_gmf
                            / 1000
                        ),
                        0
                    )
                    ELSE 0
                END AS gmf_asociado,

                /*
                 * =============================================
                 * RETIROS
                 * VB:
                 * 51,78,84,85
                 *
                 * ERP:
                 * 551,778,884,885
                 * =============================================
                 */
                CASE
                    WHEN tipo_movimiento IN (
                        '551',
                        '778',
                        '884',
                        '885'
                    )
                    THEN valor_gravado_retiro
                    ELSE 0
                END AS base_retiro,

                CASE
                    WHEN tipo_movimiento IN (
                        '551',
                        '778',
                        '884',
                        '885'
                    )
                    THEN ROUND(
                        valor_gravado_retiro
                        *
                        (
                            porcentaje_gmf
                            / 1000
                        ),
                        0
                    )
                    ELSE 0
                END AS gmf_retiro,

                CASE
                    WHEN tipo_movimiento IN (
                        '551',
                        '778',
                        '884',
                        '885'
                    )
                    THEN valor_exento_retiro
                    ELSE 0
                END AS valor_exento,

                /*
                 * =============================================
                 * CHEQUES
                 * VB: 81
                 * ERP: 881
                 * =============================================
                 */
                CASE
                    WHEN tipo_movimiento = '881'
                    THEN valor_gravado_retiro
                    ELSE 0
                END AS base_cheque_asumido,

                CASE
                    WHEN tipo_movimiento = '881'
                    THEN ROUND(
                        valor_gravado_retiro
                        *
                        (
                            porcentaje_gmf
                            / 1000
                        ),
                        0
                    )
                    ELSE 0
                END AS gmf_cheque_asumido,

                CASE
                    WHEN tipo_movimiento = '881'
                    THEN valor_exento_retiro
                    ELSE 0
                END AS base_cheque_exento,

                /*
                 * El VB también calcula el GMF teórico
                 * correspondiente a la parte exenta del cheque.
                 */
                CASE
                    WHEN tipo_movimiento = '881'
                    THEN ROUND(
                        valor_exento_retiro
                        *
                        (
                            porcentaje_gmf
                            / 1000
                        ),
                        0
                    )
                    ELSE 0
                END AS gmf_cheque_exento

            FROM bases

            WHERE
                   tipo_movimiento IN ('223', '115')

                OR tipo_movimiento IN (
                    '771',
                    '772',
                    '773',
                    '880',
                    '774',
                    '555',
                    '887'
                )

                OR tipo_movimiento IN (
                    '551',
                    '778',
                    '884',
                    '885'
                )

                OR tipo_movimiento = '881'

            ORDER BY
                codigo_forma,
                fecha_movimiento,
                hora_movimiento,
                codigo_cuenta,
                id_extracto_cuenta_ahorro
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        GmfSemanalItemDTO.builder()
                                .fechaMovimiento(
                                        rs.getObject(
                                                "fecha_movimiento",
                                                java.time.LocalDate.class
                                        )
                                )
                                .codigoCuenta(
                                        rs.getString(
                                                "codigo_cuenta"
                                        )
                                )
                                .tipoMovimiento(
                                        rs.getString(
                                                "tipo_movimiento"
                                        )
                                )
                                .descripcionMovimiento(
                                        rs.getString(
                                                "descripcion_movimiento"
                                        )
                                )
                                .documentoSoporte(
                                        rs.getString(
                                                "documento_soporte"
                                        )
                                )
                                .documentoAsociado(
                                        rs.getString(
                                                "documento_asociado"
                                        )
                                )
                                .nombreAsociado(
                                        rs.getString(
                                                "nombre_asociado"
                                        )
                                )
                                .codigoForma(
                                        rs.getString(
                                                "codigo_forma"
                                        )
                                )
                                .nombreForma(
                                        rs.getString(
                                                "nombre_forma"
                                        )
                                )
                                .estadoGmfCuenta(
                                        rs.getString(
                                                "estado_gmf_cuenta"
                                        )
                                )
                                .baseAsumido(
                                        rs.getBigDecimal(
                                                "base_asumido"
                                        )
                                )
                                .gmfAsumido(
                                        rs.getBigDecimal(
                                                "gmf_asumido"
                                        )
                                )
                                .baseAsociado(
                                        rs.getBigDecimal(
                                                "base_asociado"
                                        )
                                )
                                .gmfAsociado(
                                        rs.getBigDecimal(
                                                "gmf_asociado"
                                        )
                                )
                                .baseRetiro(
                                        rs.getBigDecimal(
                                                "base_retiro"
                                        )
                                )
                                .gmfRetiro(
                                        rs.getBigDecimal(
                                                "gmf_retiro"
                                        )
                                )
                                .valorExento(
                                        rs.getBigDecimal(
                                                "valor_exento"
                                        )
                                )
                                .baseChequeAsumido(
                                        rs.getBigDecimal(
                                                "base_cheque_asumido"
                                        )
                                )
                                .gmfChequeAsumido(
                                        rs.getBigDecimal(
                                                "gmf_cheque_asumido"
                                        )
                                )
                                .baseChequeExento(
                                        rs.getBigDecimal(
                                                "base_cheque_exento"
                                        )
                                )
                                .gmfChequeExento(
                                        rs.getBigDecimal(
                                                "gmf_cheque_exento"
                                        )
                                )
                                .build(),

                request.getFechaInicial(),
                request.getFechaFinal(),

                idAgencia,
                idAgencia,

                codigoForma,
                codigoForma
        );
    }
}
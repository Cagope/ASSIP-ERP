
package co.assip.erp.ses.formato9013;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class Formato9013Repository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Consulta los aportes sociales a una fecha de corte.
     *
     * Fuentes:
     * - Cierres mensuales definitivos de Depósitos.
     * - Extractos únicamente para conceptos históricos
     *   que no están almacenados en el cierre.
     * - Hoja de Vida para identificar al asociado.
     *
     * Consolida todas las agencias por asociado.
     */
    public List<Map<String, Object>> consultar(
            String fechaCorte
    ) {

        LocalDate fecha = LocalDate.parse(fechaCorte);

        String sql = """
            WITH cierre AS (
                SELECT
                    cm.id_cierre_mensual,
                    cm.fecha_cierre
                FROM depositos.cierres_mensuales cm
                WHERE cm.fecha_cierre = :fechaCorte
                  AND cm.estado_cierre = 'C'
            ),

            cuentas AS (
                SELECT
                    d.id_cuenta_ahorro,
                    d.id_datos_personal,
                    d.id_agencia,
                    d.saldo_cierre,
                    d.fecha_apertura_cuenta
                FROM depositos.cierres_mensuales_detalle d
                INNER JOIN cierre c
                    ON c.id_cierre_mensual =
                       d.id_cierre_mensual
                WHERE TRIM(d.codigo_forma) = '01'
            ),

            movimientos_diarios AS (
                SELECT
                    e.id_cuenta_ahorro,
                    e.fecha_movimiento::date AS fecha,
                    SUM(
                        COALESCE(e.valor_credito, 0)
                        - COALESCE(e.valor_debito, 0)
                    ) AS movimiento_neto
                FROM depositos.extractos_cuentas_ahorros e
                INNER JOIN cuentas c
                    ON c.id_cuenta_ahorro =
                       e.id_cuenta_ahorro
                WHERE e.fecha_movimiento <= :fechaCorte
                GROUP BY
                    e.id_cuenta_ahorro,
                    e.fecha_movimiento::date
            ),

            saldo_inicial AS (
                SELECT
                    c.id_cuenta_ahorro,
                    COALESCE(
                        SUM(m.movimiento_neto),
                        0
                    ) AS saldo
                FROM cuentas c
                LEFT JOIN movimientos_diarios m
                    ON m.id_cuenta_ahorro =
                       c.id_cuenta_ahorro
                   AND m.fecha < (
                       CAST(:fechaCorte AS DATE) - 364
                   )
                GROUP BY
                    c.id_cuenta_ahorro
            ),

            movimientos_rango AS (
                SELECT
                    m.id_cuenta_ahorro,
                    m.fecha,
                    m.movimiento_neto
                FROM movimientos_diarios m
                WHERE m.fecha >= (
                    CAST(:fechaCorte AS DATE) - 364
                )
            ),

            saldos_diarios AS (
                SELECT
                    m.id_cuenta_ahorro,
                    m.fecha,
                    si.saldo
                    + SUM(m.movimiento_neto) OVER (
                        PARTITION BY m.id_cuenta_ahorro
                        ORDER BY m.fecha
                        ROWS BETWEEN UNBOUNDED PRECEDING
                            AND CURRENT ROW
                    ) AS saldo
                FROM movimientos_rango m
                INNER JOIN saldo_inicial si
                    ON si.id_cuenta_ahorro =
                       m.id_cuenta_ahorro
            ),

            intervalos AS (
                SELECT
                    s.id_cuenta_ahorro,
                    s.fecha,
                    s.saldo,
                    LEAD(
                        s.fecha,
                        1,
                        CAST(:fechaCorte AS DATE) + 1
                    ) OVER (
                        PARTITION BY s.id_cuenta_ahorro
                        ORDER BY s.fecha
                    ) AS fecha_siguiente
                FROM saldos_diarios s
            ),

            promedio_movimientos AS (
                SELECT
                    i.id_cuenta_ahorro,
                    SUM(
                        i.saldo
                        * (
                            i.fecha_siguiente - i.fecha
                        )
                    ) AS saldo_ponderado
                FROM intervalos i
                GROUP BY
                    i.id_cuenta_ahorro
            ),

            primer_movimiento AS (
                SELECT
                    id_cuenta_ahorro,
                    MIN(fecha) AS primera_fecha
                FROM movimientos_rango
                GROUP BY id_cuenta_ahorro
            ),

            promedio AS (
                SELECT
                    c.id_cuenta_ahorro,
                    ROUND(
                        (
                            COALESCE(
                                pm.saldo_ponderado,
                                0
                            )
                            +
                            si.saldo
                            * (
                                COALESCE(
                                    p.primera_fecha,
                                    CAST(:fechaCorte AS DATE) + 1
                                )
                                -
                                (
                                    CAST(:fechaCorte AS DATE)
                                    - 364
                                )
                            )
                        )::numeric / 365,
                        3
                    ) AS promedio_dia_anual
                FROM cuentas c
                INNER JOIN saldo_inicial si
                    ON si.id_cuenta_ahorro =
                       c.id_cuenta_ahorro
                LEFT JOIN promedio_movimientos pm
                    ON pm.id_cuenta_ahorro =
                       c.id_cuenta_ahorro
                LEFT JOIN primer_movimiento p
                    ON p.id_cuenta_ahorro =
                       c.id_cuenta_ahorro
            ),

            revalorizaciones AS (
                SELECT
                    e.id_cuenta_ahorro,
                    SUM(
                        COALESCE(e.valor_credito, 0)
                        - COALESCE(e.valor_debito, 0)
                    ) AS valor_revalorizacion
                FROM depositos.extractos_cuentas_ahorros e
                INNER JOIN cuentas c
                    ON c.id_cuenta_ahorro =
                       e.id_cuenta_ahorro
                WHERE e.fecha_movimiento <= :fechaCorte
                  AND TRIM(e.tipo_movimiento) = '005'
                GROUP BY
                    e.id_cuenta_ahorro
            ),

            ultimos_movimientos AS (
                SELECT
                    e.id_cuenta_ahorro,
                    MAX(
                        e.fecha_movimiento::date
                    ) AS ultima_fecha
                FROM depositos.extractos_cuentas_ahorros e
                INNER JOIN cuentas c
                    ON c.id_cuenta_ahorro =
                       e.id_cuenta_ahorro
                WHERE e.fecha_movimiento <= :fechaCorte
                  AND COALESCE(e.valor_credito, 0) > 0
                GROUP BY
                    e.id_cuenta_ahorro
            ),

            aportes AS (
                SELECT
                    c.id_datos_personal,

                    COUNT(*) AS cantidad_cuentas,

                    SUM(
                        COALESCE(c.saldo_cierre, 0)
                    ) AS saldo_aportes,

                    SUM(
                        COALESCE(
                            r.valor_revalorizacion,
                            0
                        )
                    ) AS valor_revalorizacion,

                    SUM(
                        COALESCE(
                            p.promedio_dia_anual,
                            0
                        )
                    ) AS monto_promedio,

                    MAX(
                        u.ultima_fecha
                    ) AS ultima_fecha

                FROM cuentas c

                LEFT JOIN promedio p
                    ON p.id_cuenta_ahorro =
                       c.id_cuenta_ahorro

                LEFT JOIN revalorizaciones r
                    ON r.id_cuenta_ahorro =
                       c.id_cuenta_ahorro

                LEFT JOIN ultimos_movimientos u
                    ON u.id_cuenta_ahorro =
                       c.id_cuenta_ahorro

                GROUP BY
                    c.id_datos_personal

                HAVING SUM(
                    COALESCE(c.saldo_cierre, 0)
                ) > 0
            ),

            entidad AS (
                SELECT
                    TRIM(
                        e.codigo_supersolidaria
                    ) AS codigo_entidad
                FROM general.empresas e
                WHERE e.codigo_supersolidaria IS NOT NULL
                  AND TRIM(
                      e.codigo_supersolidaria
                  ) <> ''
                ORDER BY
                    e.documento_empresa
                LIMIT 1
            )

            SELECT
                en.codigo_entidad
                    AS "codigoEntidad",

                TO_CHAR(
                    ci.fecha_cierre,
                    'DDMMYYYY'
                ) AS "fechaCorte",

                ROW_NUMBER() OVER (
                    ORDER BY
                        hv.documento,
                        a.id_datos_personal
                ) AS "consecutivo",

                hv.tipo_documento
                    AS "tipoIdentificacion",

                hv.documento
                    AS "numeroIdentificacion",

                hv.primer_apellido
                    AS "primerApellido",

                hv.segundo_apellido
                    AS "segundoApellido",

                hv.nombres
                    AS "nombres",

                a.id_datos_personal
                    AS "idDatosPersonal",

                a.cantidad_cuentas
                    AS "cantidadCuentas",

                a.saldo_aportes
                    AS "saldoAportes",

                CAST(NULL AS NUMERIC)
                    AS "valorAporteMensual",

                (
                    a.saldo_aportes
                    - a.valor_revalorizacion
                ) AS "aportesOrdinarios",

                CAST(NULL AS NUMERIC)
                    AS "aportesExtraordinarios",

                a.valor_revalorizacion
                    AS "valorRevalorizacion",

                a.monto_promedio
                    AS "montoPromedio",

                TO_CHAR(
                    a.ultima_fecha,
                    'DDMMYYYY'
                ) AS "ultimaFecha",

                CAST(NULL AS NUMERIC)
                    AS "contribuciones"

            FROM aportes a

            INNER JOIN reporting.vw_hoja_vida_general_total_extendida hv
                ON hv.id_datos_personal =
                   a.id_datos_personal

            CROSS JOIN cierre ci

            CROSS JOIN entidad en

            ORDER BY
                hv.documento,
                a.id_datos_personal
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fecha
                        );

        return jdbc.queryForList(
                sql,
                parametros
        );
    }

    /**
     * Verifica la existencia de un cierre mensual
     * definitivo para la fecha solicitada.
     */
    public boolean existeCierreDefinitivo(
            String fechaCorte
    ) {

        LocalDate fecha = LocalDate.parse(fechaCorte);

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM depositos.cierres_mensuales
                WHERE fecha_cierre = :fechaCorte
                  AND estado_cierre = 'C'
            )
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fecha
                        );

        Boolean existe = jdbc.queryForObject(
                sql,
                parametros,
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }
}

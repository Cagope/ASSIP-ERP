package co.assip.erp.superintendencia.aportes.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AportesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<Map<String, Object>> consultar(String fechaCorte) {

        String sql = """
            WITH datos AS (
                SELECT 
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.id_datos_personal,
                    hv.documento AS cedula,
                    hv.tipo_documento AS tipo_identificacion,
                    hv.nombre_completo_apellidos AS nombre,
                    hv.fecha_apertura
                FROM depositos.cuentas_ahorro c
                JOIN reporting.vw_hoja_vida_general_total_extendida hv
                    ON hv.id_datos_personal = c.id_datos_personal
                WHERE c.codigo_forma = '01'
            ),

            saldos AS (
                SELECT
                    d.id_cuenta_ahorro,
                    SUM(e.valor_debito - e.valor_credito) AS saldo
                FROM datos d
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = d.id_cuenta_ahorro
                   AND e.fecha_movimiento <= CAST(:fechaCorte AS DATE)
                GROUP BY d.id_cuenta_ahorro
            ),

            reval AS (
                SELECT
                    d.id_cuenta_ahorro,
                    SUM(e.valor_debito) AS revalorizacion
                FROM datos d
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = d.id_cuenta_ahorro
                WHERE e.tipo_movimiento = '005'
                  AND e.fecha_movimiento <= CAST(:fechaCorte AS DATE)
                GROUP BY d.id_cuenta_ahorro
            ),

            ult_fecha AS (
                SELECT
                    d.id_cuenta_ahorro,
                    MAX(e.fecha_movimiento) AS fecha_ultimo_pago
                FROM datos d
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = d.id_cuenta_ahorro
                   AND e.fecha_movimiento <= CAST(:fechaCorte AS DATE)
                GROUP BY d.id_cuenta_ahorro
            ),

            movs AS (
                SELECT
                    d.id_cuenta_ahorro,
                    e.fecha_movimiento,
                    SUM(e.valor_debito - e.valor_credito)
                        OVER (
                            PARTITION BY d.id_cuenta_ahorro
                            ORDER BY e.fecha_movimiento
                        ) AS saldo
                FROM datos d
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = d.id_cuenta_ahorro
                WHERE e.fecha_movimiento <= CAST(:fechaCorte AS DATE)
            ),

            saldo_inicial AS (
                SELECT
                    d.id_cuenta_ahorro,
                    COALESCE(SUM(e.valor_debito - e.valor_credito),0) AS saldo
                FROM datos d
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = d.id_cuenta_ahorro
                WHERE e.fecha_movimiento <
                        (CAST(:fechaCorte AS DATE) - INTERVAL '364 days')
                GROUP BY d.id_cuenta_ahorro
            ),

            movs_rango AS (
                SELECT
                    m.*,
                    LEAD(
                        m.fecha_movimiento, 1,
                        CAST(:fechaCorte AS DATE) + 1
                    ) OVER (
                        PARTITION BY m.id_cuenta_ahorro
                        ORDER BY m.fecha_movimiento
                    ) AS fecha_siguiente
                FROM movs m
                WHERE m.fecha_movimiento >=
                        (CAST(:fechaCorte AS DATE) - INTERVAL '364 days')
            ),

            intervalos AS (
                SELECT
                    id_cuenta_ahorro,
                    saldo,
                    fecha_movimiento,
                    fecha_siguiente,
                    (fecha_siguiente::date - fecha_movimiento::date) AS dias
                FROM movs_rango
            ),

            primer_mov AS (
                SELECT
                    d.id_cuenta_ahorro,
                    MIN(m.fecha_movimiento) AS primera_fecha
                FROM datos d
                LEFT JOIN movs_rango m
                    ON m.id_cuenta_ahorro = d.id_cuenta_ahorro
                GROUP BY d.id_cuenta_ahorro
            ),

            tramo_inicial AS (
                SELECT
                    d.id_cuenta_ahorro,
                    si.saldo AS saldo,
                    (CAST(:fechaCorte AS DATE) - INTERVAL '364 days') AS fecha_movimiento,
                    COALESCE(pm.primera_fecha, CAST(:fechaCorte AS DATE)) AS fecha_siguiente,
                    (
                        COALESCE(pm.primera_fecha, CAST(:fechaCorte AS DATE))::date
                        -
                        (CAST(:fechaCorte AS DATE) - INTERVAL '364 days')::date
                    ) AS dias
                FROM datos d
                LEFT JOIN saldo_inicial si
                    ON si.id_cuenta_ahorro = d.id_cuenta_ahorro
                LEFT JOIN primer_mov pm
                    ON pm.id_cuenta_ahorro = d.id_cuenta_ahorro
            ),

            promedio AS (
                SELECT
                    id_cuenta_ahorro,
                    ROUND(
                        SUM(saldo * dias)::numeric / 365,
                    3) AS promedio_dia_anual
                FROM (
                    SELECT id_cuenta_ahorro, saldo, dias FROM intervalos
                    UNION ALL
                    SELECT id_cuenta_ahorro, saldo, dias FROM tramo_inicial
                ) t
                GROUP BY id_cuenta_ahorro
            )

            SELECT
                d.id_cuenta_ahorro,
                d.codigo_cuenta,
                d.id_datos_personal,
                d.cedula,
                d.tipo_identificacion,
                d.nombre,
                d.fecha_apertura,
                s.saldo,
                COALESCE(r.revalorizacion,0) AS revalorizacion,
                (s.saldo - COALESCE(r.revalorizacion,0)) AS aportes_ordinarios,
                COALESCE(u.fecha_ultimo_pago::text, 'NO FECHA') AS fecha_ultimo_pago,
                p.promedio_dia_anual
            FROM datos d
            LEFT JOIN saldos s ON s.id_cuenta_ahorro = d.id_cuenta_ahorro
            LEFT JOIN reval r ON r.id_cuenta_ahorro = d.id_cuenta_ahorro
            LEFT JOIN ult_fecha u ON u.id_cuenta_ahorro = d.id_cuenta_ahorro
            LEFT JOIN promedio p ON p.id_cuenta_ahorro = d.id_cuenta_ahorro
            ORDER BY d.id_cuenta_ahorro;
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("fechaCorte", fechaCorte);

        return jdbc.queryForList(sql, params);
    }
}

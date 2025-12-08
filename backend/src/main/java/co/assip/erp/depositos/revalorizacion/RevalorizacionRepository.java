package co.assip.erp.depositos.revalorizacion;

import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionEntradaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 📌 RevalorizacionRepository
 * ----------------------------------------------------
 * Ejecuta el SQL técnico para calcular la revalorización
 * de aportes (forma 01) por agencia y rango de fechas.
 *
 * Devuelve un listado dinámico con:
 *  - Tipo documento
 *  - Documento
 *  - Nombre completo
 *  - Saldo actual
 *  - Valor promedio
 *  - Valor revalorización
 *  - Estado cuenta (decodificado)
 */
@Repository
@RequiredArgsConstructor
public class RevalorizacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<Map<String, Object>> calcular(RevalorizacionEntradaDTO input) {

        String sql = """
            WITH datos AS (
                SELECT 
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.id_datos_personal,
                    c.id_agencia,
                    c.estado_cuenta_cuenta AS codigo_estado,
                    ea.descripcion_estado_ahorro AS estado_cuenta,
                    hv.tipo_documento,
                    hv.documento,
                    hv.nombre_completo_apellidos AS nombre_completo
                FROM depositos.cuentas_ahorro c
                JOIN reporting.vw_hoja_vida_general_total_extendida hv
                    ON hv.id_datos_personal = c.id_datos_personal
                LEFT JOIN depositos.estados_ahorros ea
                    ON ea.codigo_estado_ahorro = c.estado_cuenta_cuenta
                WHERE c.id_forma_ahorro = :formaId
                  AND c.id_agencia = :agenciaId
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
                   AND e.fecha_movimiento <= :fechaContabilizacion
            ),

            saldo_actual AS (
                SELECT
                    id_cuenta_ahorro,
                    saldo AS saldo_actual
                FROM (
                    SELECT 
                        id_cuenta_ahorro,
                        saldo,
                        ROW_NUMBER() OVER (PARTITION BY id_cuenta_ahorro ORDER BY fecha_movimiento DESC) AS rn
                    FROM movs
                ) t
                WHERE rn = 1
            ),

            saldo_inicial AS (
                SELECT
                    d.id_cuenta_ahorro,
                    COALESCE(SUM(e.valor_debito - e.valor_credito), 0) AS saldo_inicial
                FROM datos d
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = d.id_cuenta_ahorro
                   AND e.fecha_movimiento < :fechaInicio
                GROUP BY d.id_cuenta_ahorro
            ),

            movs_rango AS (
                SELECT
                    m.*,
                    LEAD(
                        m.fecha_movimiento, 
                        1, 
                        :fechaFin + INTERVAL '1 day'
                    ) OVER (
                        PARTITION BY m.id_cuenta_ahorro ORDER BY m.fecha_movimiento
                    ) AS fecha_siguiente
                FROM movs m
                WHERE m.fecha_movimiento BETWEEN :fechaInicio AND :fechaFin
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
                LEFT JOIN movs m
                  ON m.id_cuenta_ahorro = d.id_cuenta_ahorro
                 AND m.fecha_movimiento BETWEEN :fechaInicio AND :fechaFin
                GROUP BY d.id_cuenta_ahorro
            ),

            tramo_inicial AS (
                SELECT
                    d.id_cuenta_ahorro,
                    si.saldo_inicial AS saldo,
                    :fechaInicio AS fecha_movimiento,
                    COALESCE(pm.primera_fecha, :fechaFin) AS fecha_siguiente,
                    (
                        COALESCE(pm.primera_fecha, :fechaFin)::date
                        - :fechaInicio::date
                    ) AS dias
                FROM datos d
                LEFT JOIN saldo_inicial si ON si.id_cuenta_ahorro = d.id_cuenta_ahorro
                LEFT JOIN primer_mov pm ON pm.id_cuenta_ahorro = d.id_cuenta_ahorro
            ),

            promedio AS (
                SELECT
                    id_cuenta_ahorro,
                    ROUND(
                        SUM(saldo * dias)::numeric 
                        / ( (:fechaFin::date - :fechaInicio::date) + 1 ),
                    3) AS valor_promedio
                FROM (
                    SELECT id_cuenta_ahorro, saldo, dias FROM intervalos
                    UNION ALL
                    SELECT id_cuenta_ahorro, saldo, dias FROM tramo_inicial
                ) t
                GROUP BY id_cuenta_ahorro
            )

            SELECT
                d.tipo_documento,
                d.documento,
                d.nombre_completo,
                sa.saldo_actual,
                p.valor_promedio,
                ROUND((p.valor_promedio * :tasa / 100), 0) AS valor_revalorizacion,
                d.estado_cuenta
            FROM datos d
            JOIN saldo_actual sa ON sa.id_cuenta_ahorro = d.id_cuenta_ahorro
            JOIN promedio p      ON p.id_cuenta_ahorro = d.id_cuenta_ahorro
            WHERE sa.saldo_actual > 0
            ORDER BY d.documento;
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("agenciaId", input.getAgenciaId());
        params.put("fechaInicio", input.getFechaInicio());
        params.put("fechaFin", input.getFechaFin());
        params.put("fechaContabilizacion", input.getFechaContabilizacion());
        params.put("tasa", input.getTasaRevalorizacion());
        params.put("formaId", input.getFormaId());

        return jdbc.queryForList(sql, params);
    }
}

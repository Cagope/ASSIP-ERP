package co.assip.erp.depositos.interesmensual_tac;

import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACEntradaDTO;
import co.assip.erp.depositos.interesmensual_tac.dto.InteresMensualTACItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InteresMensualTACRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<InteresMensualTACItemDTO> liquidar(InteresMensualTACEntradaDTO dto) {

        String sql = """
            WITH param AS (
                SELECT
                    MAX(CASE WHEN codigo_parametro = 5 THEN valor_parametro::numeric END) AS umbral,
                    MAX(CASE WHEN codigo_parametro = 6 THEN valor_parametro::numeric END) AS porcentaje
                FROM general.parametros
                WHERE id_agencia = :agenciaId
            ),

            cuentas_tac AS (
                SELECT 
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.tasa,
                    c.id_datos_personal,
                    c.id_agencia
                FROM depositos.cuentas_ahorro c
                WHERE c.id_forma_ahorro IN (7, 14)
                  AND c.id_agencia = :agenciaId
            ),

            movs AS (
                SELECT
                    c.id_cuenta_ahorro,
                    e.fecha_movimiento,
                    SUM(e.valor_debito - e.valor_credito)
                        OVER (PARTITION BY c.id_cuenta_ahorro ORDER BY e.fecha_movimiento)
                        AS saldo
                FROM cuentas_tac c
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                   AND e.fecha_movimiento <= :fechaLiquidacion
            ),

            saldo_actual AS (
                SELECT id_cuenta_ahorro, saldo
                FROM (
                    SELECT
                        id_cuenta_ahorro,
                        saldo,
                        ROW_NUMBER() OVER (PARTITION BY id_cuenta_ahorro ORDER BY fecha_movimiento DESC) AS rn
                    FROM movs
                ) x
                WHERE rn = 1
            ),

            rango_dias AS (
                SELECT generate_series(
                    date_trunc('month', :fechaProceso::date),
                    :fechaProceso::date,
                    INTERVAL '1 day'
                )::date AS fecha
            ),

            saldos_dia AS (
                SELECT
                    c.id_cuenta_ahorro,
                    r.fecha,
                    COALESCE(
                        (
                            SELECT m.saldo
                            FROM movs m
                            WHERE m.id_cuenta_ahorro = c.id_cuenta_ahorro
                              AND m.fecha_movimiento <= r.fecha
                            ORDER BY m.fecha_movimiento DESC
                            LIMIT 1
                        ), 0
                    ) AS saldo_dia
                FROM cuentas_tac c
                CROSS JOIN rango_dias r
            ),

            promedio AS (
                SELECT
                    id_cuenta_ahorro,
                    ROUND(AVG(saldo_dia)::numeric, 2) AS promedio_mensual
                FROM saldos_dia
                GROUP BY id_cuenta_ahorro
            ),

            datos_persona AS (
                SELECT 
                    hv.id_datos_personal,
                    hv.documento,
                    hv.nombre_completo_apellidos AS nombre_completo
                FROM reporting.vw_hoja_vida_general_total_extendida hv
            )

            SELECT
                c.id_cuenta_ahorro,
                c.codigo_cuenta,
                dp.documento,
                dp.nombre_completo,

                p.promedio_mensual,
                c.tasa,

                /* ================================
                   ✔ INTERÉS BRUTO
                   (Promedio * Tasa * 30) / 36000
                   ================================ */
                ROUND(((p.promedio_mensual * c.tasa * 30) / 36000.0)::numeric, 0) AS interes_bruto,

                /* ================================
                   ✔ RETENCIÓN
                   ================================ */
                CASE 
                    WHEN ((p.promedio_mensual * c.tasa * 30) / 36000.0) >= pr.umbral
                    THEN ROUND((((p.promedio_mensual * c.tasa * 30) / 36000.0) * (pr.porcentaje / 100.0))::numeric, 0)
                    ELSE 0
                END AS retencion,

                /* ================================
                   ✔ INTERÉS NETO
                   ================================ */
                CASE 
                    WHEN ((p.promedio_mensual * c.tasa * 30) / 36000.0) >= pr.umbral
                    THEN ROUND((((p.promedio_mensual * c.tasa * 30) / 36000.0) * (1 - pr.porcentaje / 100.0))::numeric, 0)
                    ELSE ROUND(((p.promedio_mensual * c.tasa * 30) / 36000.0)::numeric, 0)
                END AS interes_neto,

                s.saldo AS saldo_actual,

                CASE 
                    WHEN ((p.promedio_mensual * c.tasa * 30) / 36000.0) >= pr.umbral
                    THEN TRUE
                    ELSE FALSE
                END AS aplica_retencion

            FROM cuentas_tac c
            JOIN promedio p       ON p.id_cuenta_ahorro = c.id_cuenta_ahorro
            JOIN saldo_actual s   ON s.id_cuenta_ahorro = c.id_cuenta_ahorro
            JOIN datos_persona dp ON dp.id_datos_personal = c.id_datos_personal
            CROSS JOIN param pr
            WHERE s.saldo > 0

            ORDER BY dp.documento;
        """;

        var params = new MapSqlParameterSource()
                .addValue("agenciaId", dto.getAgenciaId())
                .addValue("fechaProceso", dto.getFechaProceso())
                .addValue("fechaLiquidacion", dto.getFechaLiquidacion());

        return jdbc.query(sql, params, (rs, rowNum) -> {
            InteresMensualTACItemDTO d = new InteresMensualTACItemDTO();

            d.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            d.setCodigoCuenta(rs.getString("codigo_cuenta"));

            d.setDocumento(rs.getString("documento"));
            d.setNombreCompleto(rs.getString("nombre_completo"));

            d.setPromedioMensual(rs.getBigDecimal("promedio_mensual"));
            d.setTasa(rs.getBigDecimal("tasa"));

            d.setInteresBruto(rs.getBigDecimal("interes_bruto"));
            d.setRetencion(rs.getBigDecimal("retencion"));
            d.setInteresNeto(rs.getBigDecimal("interes_neto"));

            d.setSaldoActual(rs.getBigDecimal("saldo_actual"));
            d.setAplicaRetencion(rs.getBoolean("aplica_retencion"));

            return d;
        });
    }
}

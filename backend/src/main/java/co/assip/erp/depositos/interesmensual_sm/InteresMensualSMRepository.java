package co.assip.erp.depositos.interesmensual_sm;

import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMEntradaDTO;
import co.assip.erp.depositos.interesmensual_sm.dto.InteresMensualSMItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InteresMensualSMRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<InteresMensualSMItemDTO> liquidar(InteresMensualSMEntradaDTO input) {

        String sql = """
            WITH parametros AS (
                SELECT
                    :fechaProceso::date     AS fecha_proceso,
                    :fechaLiquidacion::date AS fecha_liquidacion,
                    date_trunc('month', :fechaLiquidacion::date)::date AS fecha_ini
            ),

            /* ✔ Cuentas filtradas por agencia y forma */
            cuenta AS (
                SELECT
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.id_forma_ahorro,
                    c.id_datos_personal,
                    c.retencion_fuente_cuenta,
                    hv.documento,
                    hv.nombre_completo_apellidos AS nombre
                FROM depositos.cuentas_ahorro c
                JOIN reporting.vw_hoja_vida_general_total_extendida hv
                     ON hv.id_datos_personal = c.id_datos_personal
                WHERE c.id_forma_ahorro = :formaId
                  AND c.id_agencia      = :agenciaId
            ),

            /* ✔ Datos de forma */
            forma AS (
                SELECT
                    id_forma_ahorro,
                    codigo_forma,
                    nombre_forma,
                    tasa_interes_forma,
                    tiempo_liquidacion,
                    valor_minimo
                FROM depositos.formas_ahorro
                WHERE id_forma_ahorro = :formaId
            ),

            /* ✔ Parámetros tributarios */
            param AS (
                SELECT
                    MAX(CASE WHEN codigo_parametro = 5 THEN valor_parametro::numeric END) AS umbral_retencion,
                    MAX(CASE WHEN codigo_parametro = 6 THEN valor_parametro::numeric END) AS porcentaje_retencion
                FROM general.parametros
                WHERE id_agencia = :agenciaId
            ),

            /* ✔ Saldo inicial real (antes del mes) */
            saldo_inicial AS (
                SELECT
                    c.id_cuenta_ahorro,
                    COALESCE(
                        SUM(e.valor_debito) - SUM(e.valor_credito),
                        0
                    ) AS saldo_inicial
                FROM cuenta c
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                       ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                      AND e.fecha_movimiento < (SELECT fecha_ini FROM parametros)
                GROUP BY 1
            ),

            /* ✔ Movimientos reales dentro del mes */
            movs_mes AS (
                SELECT
                    e.id_cuenta_ahorro,
                    e.fecha_movimiento,
                    e.valor_debito,
                    e.valor_credito
                FROM depositos.extractos_cuentas_ahorros e
                JOIN cuenta c ON c.id_cuenta_ahorro = e.id_cuenta_ahorro
                WHERE e.fecha_movimiento BETWEEN
                      (SELECT fecha_ini FROM parametros)
                      AND (SELECT fecha_liquidacion FROM parametros)
            ),

            /* 🟩 Incluir saldo inicial como un movimiento artificial */
            movs_completos AS (
                SELECT
                    c.id_cuenta_ahorro,
                    (SELECT fecha_ini FROM parametros) - INTERVAL '1 day' AS fecha_movimiento,
                    saldo_inicial.saldo_inicial AS saldo_inicial,
                    0 AS valor_debito,
                    0 AS valor_credito
                FROM cuenta c
                JOIN saldo_inicial ON saldo_inicial.id_cuenta_ahorro = c.id_cuenta_ahorro

                UNION ALL

                SELECT
                    m.id_cuenta_ahorro,
                    m.fecha_movimiento,
                    NULL AS saldo_inicial,
                    m.valor_debito,
                    m.valor_credito
                FROM movs_mes m
            ),

            /* 🟩 Saldos acumulados en estricto orden de fechas */
            saldos_acumulados AS (
                SELECT
                    id_cuenta_ahorro,
                    fecha_movimiento,
                    MAX(saldo_inicial) OVER (PARTITION BY id_cuenta_ahorro)
                    + SUM(
                        COALESCE(valor_debito,0) - COALESCE(valor_credito,0)
                      ) OVER (
                            PARTITION BY id_cuenta_ahorro
                            ORDER BY fecha_movimiento
                      ) AS saldo_acumulado
                FROM movs_completos
            ),

            /* 🟩 Mínimo del mes (saldo inicial y movimientos incluidos) */
            minimos AS (
                SELECT
                    id_cuenta_ahorro,
                    MIN(saldo_acumulado) AS saldo_minimo_mes
                FROM saldos_acumulados
                GROUP BY 1
            ),

            /* ✔ Cálculo del interés mensual */
            calculo AS (
                SELECT
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.documento,
                    c.nombre,
                    mm.saldo_minimo_mes,
                    f.tasa_interes_forma,
                    f.tiempo_liquidacion,
                    f.valor_minimo AS minimo_forma,

                    ROUND(
                        (
                            (mm.saldo_minimo_mes * f.tiempo_liquidacion * f.tasa_interes_forma)
                            / 36000
                        )::numeric,
                        0
                    ) AS interes_bruto
                FROM cuenta c
                JOIN minimos mm ON mm.id_cuenta_ahorro = c.id_cuenta_ahorro
                JOIN forma f    ON f.id_forma_ahorro = c.id_forma_ahorro
            ),

            /* ✔ Validación contra el mínimo */
            interes_minimo AS (
                SELECT
                    *,
                    CASE
                        WHEN saldo_minimo_mes < minimo_forma THEN 0
                        ELSE interes_bruto
                    END AS interes_valido
                FROM calculo
            ),

            /* ✔ Aplicación de retención */
            result AS (
                SELECT
                    i.*,
                    p.umbral_retencion,
                    p.porcentaje_retencion,
                    c.retencion_fuente_cuenta,

                    CASE 
                        WHEN i.interes_valido >= p.umbral_retencion
                         AND c.retencion_fuente_cuenta = TRUE
                        THEN ROUND(i.interes_valido * p.porcentaje_retencion / 100.0, 0)
                        ELSE 0
                    END AS retencion,

                    CASE 
                        WHEN i.interes_valido >= p.umbral_retencion
                         AND c.retencion_fuente_cuenta = TRUE
                        THEN TRUE
                        ELSE FALSE
                    END AS aplica_retencion
                FROM interes_minimo i
                CROSS JOIN param p
                JOIN cuenta c ON c.id_cuenta_ahorro = i.id_cuenta_ahorro
            )

            SELECT
                id_cuenta_ahorro,
                codigo_cuenta,
                documento,
                nombre,
                saldo_minimo_mes,
                interes_bruto,
                retencion,
                (interes_valido - retencion) AS neto_pagar,
                tasa_interes_forma,
                tiempo_liquidacion,
                minimo_forma,
                aplica_retencion
            FROM result
            WHERE interes_valido > 0
              AND saldo_minimo_mes > 0
            ORDER BY codigo_cuenta;
            """;

        var params = new MapSqlParameterSource()
                .addValue("agenciaId", input.getAgenciaId())
                .addValue("fechaProceso", input.getFechaProceso())
                .addValue("fechaLiquidacion", input.getFechaLiquidacion())
                .addValue("formaId", input.getFormaId());

        return jdbc.query(sql, params, (rs, rowNum) -> {
            InteresMensualSMItemDTO dto = new InteresMensualSMItemDTO();

            dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));

            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre"));

            dto.setSaldoMinimoMes(rs.getBigDecimal("saldo_minimo_mes"));
            dto.setInteresBruto(rs.getBigDecimal("interes_bruto"));
            dto.setRetencion(rs.getBigDecimal("retencion"));
            dto.setInteresNeto(rs.getBigDecimal("neto_pagar"));

            dto.setTasaInteres(rs.getBigDecimal("tasa_interes_forma"));
            dto.setTiempoLiquidacion(rs.getInt("tiempo_liquidacion"));
            dto.setMinimoForma(rs.getBigDecimal("minimo_forma"));

            dto.setAplicaRetencion(rs.getBoolean("aplica_retencion"));

            return dto;
        });
    }
}

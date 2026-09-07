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

            hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    documento,
                    nombre_completo_apellidos AS nombre
                FROM reporting.vw_hoja_vida_general_total_extendida
                ORDER BY id_datos_personal
            ),

            cuenta AS (
                SELECT DISTINCT ON (c.id_cuenta_ahorro)
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.id_forma_ahorro,
                    c.id_datos_personal,
                    c.retencion_fuente_cuenta,
                    hv.documento,
                    hv.nombre
                FROM depositos.cuentas_ahorro c
                JOIN hv_unica hv
                     ON hv.id_datos_personal = c.id_datos_personal
                WHERE c.id_forma_ahorro = :formaId
                  AND c.id_agencia      = :agenciaId
                ORDER BY c.id_cuenta_ahorro
            ),

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

            param AS (
                SELECT
                    MAX(CASE WHEN codigo_parametro = 5 THEN valor_parametro::numeric END) AS base_retencion_diaria,
                    MAX(CASE WHEN codigo_parametro = 6 THEN valor_parametro::numeric END) AS porcentaje_retencion
                FROM general.parametros
                WHERE id_agencia = :agenciaId
            ),

            saldo_inicial AS (
                SELECT
                    c.id_cuenta_ahorro,
                    COALESCE(
                        SUM(e.valor_credito) - SUM(e.valor_debito),
                        0
                    ) AS saldo_inicial
                FROM cuenta c
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                       ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                      AND e.fecha_movimiento < (SELECT fecha_ini FROM parametros)
                GROUP BY c.id_cuenta_ahorro
            ),

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

            movs_completos AS (
                SELECT
                    c.id_cuenta_ahorro,
                    (SELECT fecha_ini FROM parametros) - INTERVAL '1 day' AS fecha_movimiento,
                    si.saldo_inicial AS saldo_inicial,
                    0::numeric AS valor_debito,
                    0::numeric AS valor_credito
                FROM cuenta c
                JOIN saldo_inicial si
                  ON si.id_cuenta_ahorro = c.id_cuenta_ahorro

                UNION ALL

                SELECT
                    m.id_cuenta_ahorro,
                    m.fecha_movimiento,
                    NULL::numeric AS saldo_inicial,
                    m.valor_debito::numeric,
                    m.valor_credito::numeric
                FROM movs_mes m
            ),

            saldos_acumulados AS (
                SELECT
                    id_cuenta_ahorro,
                    fecha_movimiento,
                    MAX(saldo_inicial) OVER (PARTITION BY id_cuenta_ahorro)
                    + SUM(
                        COALESCE(valor_credito,0) - COALESCE(valor_debito,0)
                      ) OVER (
                            PARTITION BY id_cuenta_ahorro
                            ORDER BY fecha_movimiento
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                      ) AS saldo_acumulado
                FROM movs_completos
            ),

            minimos AS (
                SELECT
                    id_cuenta_ahorro,
                    MIN(saldo_acumulado) AS saldo_minimo_mes
                FROM saldos_acumulados
                GROUP BY id_cuenta_ahorro
            ),

            calculo AS (
                SELECT
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.id_datos_personal,
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
                    ) AS interes_bruto,
                    c.retencion_fuente_cuenta
                FROM cuenta c
                JOIN minimos mm ON mm.id_cuenta_ahorro = c.id_cuenta_ahorro
                JOIN forma f    ON f.id_forma_ahorro = c.id_forma_ahorro
            ),

            interes_minimo AS (
                SELECT
                    *,
                    CASE
                        WHEN saldo_minimo_mes < minimo_forma THEN 0
                        ELSE interes_bruto
                    END AS interes_valido
                FROM calculo
            ),

            result AS (
                 SELECT
                     i.*,
                     p.base_retencion_diaria,
                     p.porcentaje_retencion,
            
                     CASE
                         WHEN FLOOR(
                                 i.interes_valido
                                 / NULLIF(i.tiempo_liquidacion, 0)
                              ) >= p.base_retencion_diaria
                          AND i.retencion_fuente_cuenta = TRUE
            
                         THEN ROUND(
                                 i.interes_valido
                                 * p.porcentaje_retencion
                                 / 100.0,
                                 0
                              )
            
                         ELSE 0
                     END AS retencion,
            
                     CASE
                         WHEN FLOOR(
                                 i.interes_valido
                                 / NULLIF(i.tiempo_liquidacion, 0)
                              ) >= p.base_retencion_diaria
                          AND i.retencion_fuente_cuenta = TRUE
                         THEN TRUE
                         ELSE FALSE
                     END AS aplica_retencion
            
                 FROM interes_minimo i
                 CROSS JOIN param p
             )

            SELECT
                id_cuenta_ahorro,
                codigo_cuenta,
                id_datos_personal,
                documento,
                nombre,
                saldo_minimo_mes,
                interes_bruto,
                retencion,
                (interes_valido - retencion) AS neto_pagar,
                tasa_interes_forma,
                tiempo_liquidacion,
                minimo_forma,
                retencion_fuente_cuenta,
                aplica_retencion
            FROM result
            WHERE interes_valido > 0
              AND saldo_minimo_mes >= minimo_forma
            ORDER BY nombre, documento, codigo_cuenta;
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
            dto.setIdDatosPersonal(rs.getInt("id_datos_personal"));
            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre"));
            dto.setSaldoMinimoMes(rs.getBigDecimal("saldo_minimo_mes"));
            dto.setInteresBruto(rs.getBigDecimal("interes_bruto"));
            dto.setRetencion(rs.getBigDecimal("retencion"));
            dto.setInteresNeto(rs.getBigDecimal("neto_pagar"));
            dto.setTasaInteres(rs.getBigDecimal("tasa_interes_forma"));
            dto.setTiempoLiquidacion(rs.getInt("tiempo_liquidacion"));
            dto.setMinimoForma(rs.getBigDecimal("minimo_forma"));
            dto.setAplicaRetencion(rs.getBoolean("retencion_fuente_cuenta"));

            return dto;
        });
    }

}
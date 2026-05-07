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

    public List<InteresMensualTACItemDTO> liquidar(InteresMensualTACEntradaDTO input) {

        String sql = """
            WITH parametros AS (
                SELECT
                    :fechaProceso::date     AS fecha_proceso,
                    :fechaLiquidacion::date AS fecha_liquidacion,
                    date_trunc('month', :fechaProceso::date)::date AS fecha_ini
            ),

            param AS (
                SELECT
                    MAX(CASE WHEN codigo_parametro = 5 THEN valor_parametro::numeric END) AS umbral,
                    MAX(CASE WHEN codigo_parametro = 6 THEN valor_parametro::numeric END) AS porcentaje
                FROM general.parametros
                WHERE id_agencia = :agenciaId
            ),

            forma AS (
                SELECT
                    id_forma_ahorro,
                    codigo_forma,
                    nombre_forma,
                    tiempo_liquidacion,
                    valor_minimo::numeric AS minimo_forma
                FROM depositos.formas_ahorro
                WHERE id_forma_ahorro = :formaId
            ),

            hv_unica AS (
                SELECT DISTINCT ON (id_datos_personal)
                    id_datos_personal,
                    documento,
                    nombre_completo_apellidos AS nombre
                FROM reporting.vw_hoja_vida_general_total_extendida
                ORDER BY id_datos_personal
            ),

            cuentas_tac AS (
                SELECT DISTINCT ON (c.id_cuenta_ahorro)
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.id_forma_ahorro,
                    c.id_datos_personal,
                    c.id_agencia,
                    c.tasa::numeric AS tasa,
                    hv.documento,
                    hv.nombre
                FROM depositos.cuentas_ahorro c
                JOIN hv_unica hv
                  ON hv.id_datos_personal = c.id_datos_personal
                WHERE c.id_forma_ahorro = :formaId
                  AND c.id_agencia = :agenciaId
                ORDER BY c.id_cuenta_ahorro
            ),

            movs AS (
                SELECT
                    c.id_cuenta_ahorro,
                    e.fecha_movimiento,
                    SUM(e.valor_debito - e.valor_credito)
                        OVER (
                            PARTITION BY c.id_cuenta_ahorro
                            ORDER BY e.fecha_movimiento
                            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                        ) AS saldo
                FROM cuentas_tac c
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                  ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                 AND e.fecha_movimiento <= (SELECT fecha_liquidacion FROM parametros)
            ),

            saldo_actual AS (
                SELECT
                    id_cuenta_ahorro,
                    saldo
                FROM (
                    SELECT
                        id_cuenta_ahorro,
                        saldo,
                        ROW_NUMBER() OVER (
                            PARTITION BY id_cuenta_ahorro
                            ORDER BY fecha_movimiento DESC
                        ) AS rn
                    FROM movs
                ) x
                WHERE rn = 1
            ),

            rango_dias AS (
                SELECT generate_series(
                    (SELECT fecha_ini FROM parametros),
                    (SELECT fecha_proceso FROM parametros),
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
                        ),
                        0
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

            calculo AS (
                SELECT
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.id_datos_personal,
                    c.documento,
                    c.nombre,
                    p.promedio_mensual,
                    c.tasa,
                    f.tiempo_liquidacion,
                    f.minimo_forma,
                    s.saldo AS saldo_actual,

                    ROUND(
                        (
                            (p.promedio_mensual * c.tasa * 30)
                            / 36000.0
                        )::numeric,
                        0
                    ) AS interes_bruto
                FROM cuentas_tac c
                JOIN promedio p
                  ON p.id_cuenta_ahorro = c.id_cuenta_ahorro
                JOIN saldo_actual s
                  ON s.id_cuenta_ahorro = c.id_cuenta_ahorro
                JOIN forma f
                  ON f.id_forma_ahorro = c.id_forma_ahorro
            ),

            result AS (
                SELECT
                    c.*,
                    pr.umbral,
                    pr.porcentaje,

                    CASE
                        WHEN c.interes_bruto >= pr.umbral
                        THEN ROUND(
                            (
                                c.interes_bruto
                                * pr.porcentaje
                                / 100.0
                            )::numeric,
                            0
                        )
                        ELSE 0
                    END AS retencion,

                    CASE
                        WHEN c.interes_bruto >= pr.umbral
                        THEN TRUE
                        ELSE FALSE
                    END AS aplica_retencion
                FROM calculo c
                CROSS JOIN param pr
            )

            SELECT
                id_cuenta_ahorro,
                codigo_cuenta,
                id_datos_personal,
                documento,
                nombre,
                promedio_mensual,
                tasa,
                interes_bruto,
                retencion,
                (interes_bruto - retencion) AS interes_neto,
                saldo_actual,
                tasa AS tasa_interes,
                tiempo_liquidacion,
                minimo_forma,
                aplica_retencion
            FROM result
            WHERE saldo_actual > 0
              AND interes_bruto > 0
            ORDER BY nombre, documento, codigo_cuenta;
            """;

        var params = new MapSqlParameterSource()
                .addValue("agenciaId", input.getAgenciaId())
                .addValue("formaId", input.getFormaId())
                .addValue("fechaProceso", input.getFechaProceso())
                .addValue("fechaLiquidacion", input.getFechaLiquidacion());

        return jdbc.query(sql, params, (rs, rowNum) -> {
            InteresMensualTACItemDTO dto = new InteresMensualTACItemDTO();

            dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
            dto.setIdDatosPersonal(rs.getInt("id_datos_personal"));

            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre"));

            dto.setPromedioMensual(rs.getBigDecimal("promedio_mensual"));
            dto.setTasa(rs.getBigDecimal("tasa"));
            dto.setSaldoActual(rs.getBigDecimal("saldo_actual"));

            dto.setInteresBruto(rs.getBigDecimal("interes_bruto"));
            dto.setRetencion(rs.getBigDecimal("retencion"));
            dto.setInteresNeto(rs.getBigDecimal("interes_neto"));

            dto.setTasaInteres(rs.getBigDecimal("tasa_interes"));
            dto.setTiempoLiquidacion(rs.getInt("tiempo_liquidacion"));
            dto.setMinimoForma(rs.getBigDecimal("minimo_forma"));

            dto.setAplicaRetencion(rs.getBoolean("aplica_retencion"));

            return dto;
        });
    }
}
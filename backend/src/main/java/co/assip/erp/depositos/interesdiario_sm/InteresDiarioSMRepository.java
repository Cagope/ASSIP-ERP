package co.assip.erp.depositos.interesdiario_sm;

import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMEntradaDTO;
import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InteresDiarioSMRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<InteresDiarioSMItemDTO> liquidar(InteresDiarioSMEntradaDTO input) {

        String sql = """
            WITH parametros AS (
                SELECT
                    :fechaProceso::date     AS fecha_proceso,
                    :fechaLiquidacion::date AS fecha_liquidacion
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
                    valor_minimo,
                    fecha_ultima_liquidacion
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

            saldo_anterior AS (
                SELECT
                    c.id_cuenta_ahorro,
                    COALESCE(
                        SUM(e.valor_debito) - SUM(e.valor_credito),
                        0
                    ) AS saldo_al_dia_anterior
                FROM cuenta c
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                       ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                      AND e.fecha_movimiento < (SELECT fecha_proceso FROM parametros)
                GROUP BY c.id_cuenta_ahorro
            ),

            movs_dia AS (
                SELECT
                    e.id_extracto_cuenta_ahorro,
                    e.id_cuenta_ahorro,
                    e.hora_movimiento,
                    e.valor_debito::numeric,
                    e.valor_credito::numeric,
                    (e.valor_debito - e.valor_credito) AS movimiento
                FROM depositos.extractos_cuentas_ahorros e
                JOIN cuenta c
                  ON c.id_cuenta_ahorro = e.id_cuenta_ahorro
                WHERE e.fecha_movimiento = (SELECT fecha_proceso FROM parametros)
            ),

            saldo_dia AS (
                SELECT
                    sa.id_cuenta_ahorro,
                    sa.saldo_al_dia_anterior
                    + SUM(md.movimiento) OVER (
                        PARTITION BY md.id_cuenta_ahorro
                        ORDER BY md.id_extracto_cuenta_ahorro
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS saldo
                FROM saldo_anterior sa
                JOIN movs_dia md
                  ON md.id_cuenta_ahorro = sa.id_cuenta_ahorro

                UNION ALL

                SELECT
                    sa.id_cuenta_ahorro,
                    sa.saldo_al_dia_anterior AS saldo
                FROM saldo_anterior sa
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM movs_dia md2
                    WHERE md2.id_cuenta_ahorro = sa.id_cuenta_ahorro
                )
            ),

            saldo_diario AS (
                SELECT
                    id_cuenta_ahorro,
                    MIN(saldo) AS saldo_minimo_dia
                FROM saldo_dia
                GROUP BY id_cuenta_ahorro
            ),

            calculo AS (
                SELECT
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.id_datos_personal,
                    c.documento,
                    c.nombre,
                    sd.saldo_minimo_dia,
                    f.tasa_interes_forma,
                    f.tiempo_liquidacion,
                    f.valor_minimo AS minimo_forma,
                    ROUND(
                        (
                            (sd.saldo_minimo_dia * f.tasa_interes_forma)
                            / 36000
                        )::numeric,
                        0
                    ) AS interes_bruto,
                    c.retencion_fuente_cuenta
                FROM cuenta c
                JOIN saldo_diario sd
                  ON sd.id_cuenta_ahorro = c.id_cuenta_ahorro
                JOIN forma f
                  ON f.id_forma_ahorro = c.id_forma_ahorro
            ),

            interes_minimo AS (
                SELECT
                    *,
                    CASE
                        WHEN saldo_minimo_dia < minimo_forma THEN 0
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
                        WHEN i.interes_valido >= p.base_retencion_diaria
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
                        WHEN i.interes_valido >= p.base_retencion_diaria
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
                saldo_minimo_dia,
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
              AND saldo_minimo_dia >= minimo_forma
            ORDER BY nombre, documento, codigo_cuenta;
            """;

        var params = new MapSqlParameterSource()
                .addValue("agenciaId", input.getAgenciaId())
                .addValue("fechaProceso", input.getFechaProceso())
                .addValue("fechaLiquidacion", input.getFechaLiquidacion())
                .addValue("formaId", input.getFormaId());

        return jdbc.query(sql, params, (rs, rowNum) -> {
            InteresDiarioSMItemDTO dto = new InteresDiarioSMItemDTO();

            dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
            dto.setIdDatosPersonal(rs.getInt("id_datos_personal"));
            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre"));
            dto.setSaldoMinimoDia(rs.getBigDecimal("saldo_minimo_dia"));
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
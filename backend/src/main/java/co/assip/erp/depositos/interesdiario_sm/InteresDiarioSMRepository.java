package co.assip.erp.depositos.interesdiario_sm;

import co.assip.erp.depositos.interesdiario_sm.dto.InteresDiarioSMItemDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class InteresDiarioSMRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public InteresDiarioSMRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<InteresDiarioSMItemDTO> simular(
            Integer agenciaId,
            LocalDate fechaProceso,
            LocalDate fechaLiquidacion,
            Integer formaId
    ) {

        String sql = """
            WITH parametros AS (
                SELECT
                    :fechaProceso::date     AS fecha_proceso,
                    :fechaLiquidacion::date AS fecha_liquidacion
            ),

            ag_info AS (
                SELECT id_agencia
                FROM general.datos_agencias
                WHERE id_agencia = :agenciaId
            ),

            /* ✔ Cuentas por forma — (PASO 1 aplicado) */
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
            ),

            /* ✔ Forma — una sola definición (PASO 2 aplicado) */
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

            /* ✔ Parámetros tributarios */
            param AS (
                SELECT
                    MAX(CASE WHEN codigo_parametro = 5 THEN valor_parametro::numeric END) AS umbral_retencion,
                    MAX(CASE WHEN codigo_parametro = 6 THEN valor_parametro::numeric END) AS porcentaje_retencion
                FROM general.parametros
                WHERE id_agencia = :agenciaId
            ),

            /* ✔ Movimientos del día */
            movs_dia AS (
                SELECT
                    e.id_extracto_cuenta_ahorro,
                    e.id_cuenta_ahorro,
                    e.hora_movimiento,
                    (e.valor_debito - e.valor_credito) AS movimiento
                FROM depositos.extractos_cuentas_ahorros e
                JOIN cuenta c ON c.id_cuenta_ahorro = e.id_cuenta_ahorro
                WHERE e.fecha_movimiento = (SELECT fecha_proceso FROM parametros)
                ORDER BY e.id_cuenta_ahorro, e.hora_movimiento
            ),

            /* ✔ Saldo al día anterior */
            saldo_anterior AS (
                SELECT
                    c.id_cuenta_ahorro,
                    COALESCE(SUM(e.valor_debito - e.valor_credito), 0) AS saldo_al_dia_anterior
                FROM cuenta c
                LEFT JOIN depositos.extractos_cuentas_ahorros e
                    ON e.id_cuenta_ahorro = c.id_cuenta_ahorro
                   AND e.fecha_movimiento < (SELECT fecha_proceso FROM parametros)
                GROUP BY c.id_cuenta_ahorro
            ),

            /* ✔ Reconstrucción del día — (PASO 3 y 5 aplicados) */
            saldo_dia AS (
                -- Si hay movimientos
                SELECT
                    sa.id_cuenta_ahorro,
                    sa.saldo_al_dia_anterior
                    + SUM(md.movimiento) OVER (
                        ORDER BY md.id_extracto_cuenta_ahorro
                    ) AS saldo
                FROM saldo_anterior sa
                JOIN movs_dia md
                    ON md.id_cuenta_ahorro = sa.id_cuenta_ahorro

                UNION ALL

                -- Si no hay movimientos para la cuenta
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

            /* ✔ Saldo mínimo del día */
            saldo_diario AS (
                SELECT
                    id_cuenta_ahorro,
                    MIN(saldo) AS saldo_minimo
                FROM saldo_dia
                GROUP BY id_cuenta_ahorro
            ),

            /* ✔ Cálculo del interés */
            calculo AS (
                SELECT
                    c.id_cuenta_ahorro,
                    c.codigo_cuenta,
                    c.documento,
                    c.nombre,
                    sd.saldo_minimo,
                    f.tasa_interes_forma,
                    f.tiempo_liquidacion,
                    f.valor_minimo AS minimo_forma,

                    ROUND(
                        (
                            (sd.saldo_minimo * f.tiempo_liquidacion * f.tasa_interes_forma) 
                            / 36000
                        )::numeric,
                        0
                    ) AS interes_bruto
                FROM cuenta c
                JOIN saldo_diario sd ON sd.id_cuenta_ahorro = c.id_cuenta_ahorro
                JOIN forma f ON f.id_forma_ahorro = c.id_forma_ahorro
            ),

            /* ✔ Validación del mínimo */
            interes_minimo AS (
                SELECT
                    *,
                    CASE 
                        WHEN saldo_minimo < minimo_forma THEN 0
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
                  saldo_minimo,
                  interes_bruto,
                  retencion,
                  (interes_valido - retencion) AS neto_pagar,
                  tasa_interes_forma,
                  tiempo_liquidacion,
                  minimo_forma,
                  aplica_retencion
            FROM result
            WHERE interes_valido > 0
              AND saldo_minimo > 0
            ORDER BY codigo_cuenta
            """;

        var params = new MapSqlParameterSource()
                .addValue("agenciaId", agenciaId)
                .addValue("fechaProceso", fechaProceso)
                .addValue("fechaLiquidacion", fechaLiquidacion)
                .addValue("formaId", formaId);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            InteresDiarioSMItemDTO dto = new InteresDiarioSMItemDTO();

            dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));

            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre"));

            dto.setSaldoMinimoDia(rs.getBigDecimal("saldo_minimo"));
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

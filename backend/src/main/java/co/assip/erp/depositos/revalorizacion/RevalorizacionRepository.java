package co.assip.erp.depositos.revalorizacion;

import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionEntradaDTO;
import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RevalorizacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<RevalorizacionItemDTO> liquidar(RevalorizacionEntradaDTO input) {

        String sql = """
        WITH hv_unica AS (
            SELECT DISTINCT ON (id_datos_personal)
                id_datos_personal,
                tipo_documento,
                documento,
                nombre_completo_apellidos AS nombre_completo
            FROM reporting.vw_hoja_vida_general_total_extendida
            ORDER BY id_datos_personal
        ),

        datos AS (
            SELECT DISTINCT ON (c.id_cuenta_ahorro)
                c.id_cuenta_ahorro,
                c.codigo_cuenta,
                c.id_datos_personal,
                c.id_agencia,
                c.estado_cuenta_cuenta AS codigo_estado,
                ea.descripcion_estado_ahorro AS estado_cuenta,
                hv.tipo_documento,
                hv.documento,
                hv.nombre_completo
            FROM depositos.cuentas_ahorro c
            JOIN hv_unica hv
                ON hv.id_datos_personal = c.id_datos_personal
            LEFT JOIN depositos.estados_ahorros ea
                ON ea.codigo_estado_ahorro = c.estado_cuenta_cuenta
            WHERE c.id_forma_ahorro = :formaId
              AND c.id_agencia = :agenciaId
              AND c.estado_cuenta_cuenta = 'A'
            ORDER BY c.id_cuenta_ahorro
        ),

        forma AS (
            SELECT
                id_forma_ahorro,
                tiempo_liquidacion,
                valor_minimo::numeric AS minimo_forma
            FROM depositos.formas_ahorro
            WHERE id_forma_ahorro = :formaId
        ),

        movs AS (
            SELECT
                d.id_cuenta_ahorro,
                e.fecha_movimiento,
                SUM(e.valor_credito - e.valor_debito)
                    OVER (
                        PARTITION BY d.id_cuenta_ahorro
                        ORDER BY e.fecha_movimiento
                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
                    ) AS saldo
            FROM datos d
            LEFT JOIN depositos.extractos_cuentas_ahorros e
                ON e.id_cuenta_ahorro = d.id_cuenta_ahorro
               AND e.fecha_movimiento <= :fechaProceso
        ),

        saldo_actual AS (
            SELECT
                id_cuenta_ahorro,
                saldo AS saldo_actual
            FROM (
                SELECT
                    id_cuenta_ahorro,
                    saldo,
                    ROW_NUMBER() OVER (
                        PARTITION BY id_cuenta_ahorro
                        ORDER BY fecha_movimiento DESC
                    ) AS rn
                FROM movs
            ) t
            WHERE rn = 1
        ),

        saldo_inicial AS (
            SELECT
                d.id_cuenta_ahorro,
                COALESCE(SUM(e.valor_credito - e.valor_debito), 0) AS saldo_inicial
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
                    PARTITION BY m.id_cuenta_ahorro
                    ORDER BY m.fecha_movimiento
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
                COALESCE(pm.primera_fecha, :fechaFin + INTERVAL '1 day') AS fecha_siguiente,
                (
                    COALESCE(pm.primera_fecha, :fechaFin + INTERVAL '1 day')::date
                    - :fechaInicio::date
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
                    SUM(saldo * dias)::numeric
                    / ((:fechaFin::date - :fechaInicio::date) + 1),
                    3
                ) AS valor_promedio
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
            d.tipo_documento,
            d.documento,
            d.nombre_completo,
            sa.saldo_actual,
            p.valor_promedio,
            ROUND((p.valor_promedio * :tasa / 100), 0) AS valor_revalorizacion,
            (sa.saldo_actual + ROUND((p.valor_promedio * :tasa / 100), 0)) AS nuevo_saldo,
            :tasa::numeric AS tasa_revalorizacion,
            f.tiempo_liquidacion,
            f.minimo_forma,
            d.estado_cuenta
        FROM datos d
        JOIN saldo_actual sa
            ON sa.id_cuenta_ahorro = d.id_cuenta_ahorro
        JOIN promedio p
            ON p.id_cuenta_ahorro = d.id_cuenta_ahorro
        JOIN forma f
            ON f.id_forma_ahorro = :formaId
        WHERE sa.saldo_actual > 0
          AND ROUND((p.valor_promedio * :tasa / 100), 0) > 0
        ORDER BY d.nombre_completo, d.documento, d.codigo_cuenta;
    """;

        var params = new MapSqlParameterSource()
                .addValue("agenciaId", input.getAgenciaId())
                .addValue("formaId", input.getFormaId())
                .addValue("fechaInicio", input.getFechaInicio())
                .addValue("fechaFin", input.getFechaFin())
                .addValue("fechaProceso", input.getFechaProceso())
                .addValue("fechaLiquidacion", input.getFechaLiquidacion())
                .addValue("tasa", input.getTasaRevalorizacion());

        return jdbc.query(sql, params, (rs, rowNum) -> {
            RevalorizacionItemDTO dto = new RevalorizacionItemDTO();

            dto.setIdCuentaAhorro(rs.getInt("id_cuenta_ahorro"));
            dto.setCodigoCuenta(rs.getString("codigo_cuenta"));
            dto.setIdDatosPersonal(rs.getInt("id_datos_personal"));

            dto.setTipoDocumento(rs.getString("tipo_documento"));
            dto.setDocumento(rs.getString("documento"));
            dto.setNombreCompleto(rs.getString("nombre_completo"));

            dto.setSaldoActual(rs.getBigDecimal("saldo_actual"));
            dto.setValorPromedio(rs.getBigDecimal("valor_promedio"));
            dto.setValorRevalorizacion(rs.getBigDecimal("valor_revalorizacion"));
            dto.setNuevoSaldo(rs.getBigDecimal("nuevo_saldo"));

            dto.setTasaRevalorizacion(rs.getBigDecimal("tasa_revalorizacion"));
            dto.setTiempoLiquidacion(rs.getInt("tiempo_liquidacion"));
            dto.setMinimoForma(rs.getBigDecimal("minimo_forma"));

            dto.setEstadoCuenta(rs.getString("estado_cuenta"));

            return dto;
        });
    }
}
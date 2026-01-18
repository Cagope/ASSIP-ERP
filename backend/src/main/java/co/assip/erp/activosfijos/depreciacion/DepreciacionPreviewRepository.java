package co.assip.erp.activosfijos.depreciacion;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DepreciacionPreviewRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DepreciacionPreviewRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * ✅ Obtiene el preview de depreciación por activo para un período dado y agencia.
     * ✅ Solo incluye activos cuyo estado permite depreciación (estados_activo.permite_depreciacion = true)
     * ✅ Trae el proveedor para usarlo como id_datos_personal en contabilidad
     */
    public List<DepreciacionPreviewDTO> obtenerPreview(Integer idAgencia) {

        String sql = """
            SELECT
                a.id_activo_fijo,
                a.placa_activo,
                a.nombre_activo,

                a.valor_mensual,

                COALESCE(SUM(e.valor_credito), 0) AS depreciacion_acumulada,

                (a.valor_adquisicion - COALESCE(SUM(e.valor_credito), 0)) AS saldo_pendiente,

                LEAST(
                    a.valor_mensual,
                    (a.valor_adquisicion - COALESCE(SUM(e.valor_credito), 0))
                ) AS valor_periodo,

                a.id_catalogo_cuenta_gasto,
                a.id_catalogo_cuenta_depreciacion,

                a.id_datos_personal_proveedor

            FROM activos_fijos.activos_fijos a

            -- ✅ Estado del activo (aquí está permite_depreciacion)
            JOIN activos_fijos.estados_activo ea
              ON ea.id_estado_activo = a.id_estado_activo
             AND ea.permite_depreciacion IS TRUE

            LEFT JOIN activos_fijos.extracto_activos e
                   ON e.id_activo_fijo = a.id_activo_fijo
                  AND e.codigo_movimiento = '71'

            WHERE a.id_agencia = :idAgencia
              AND a.fecha_ingreso <= CURRENT_DATE
              AND (a.fecha_baja IS NULL OR a.fecha_baja > CURRENT_DATE)
              AND a.meses_depreciacion > 0
              AND a.valor_adquisicion > 0

            GROUP BY
                a.id_activo_fijo,
                a.placa_activo,
                a.nombre_activo,
                a.valor_mensual,
                a.valor_adquisicion,
                a.id_catalogo_cuenta_gasto,
                a.id_catalogo_cuenta_depreciacion,
                a.id_datos_personal_proveedor

            HAVING
                (a.valor_adquisicion - COALESCE(SUM(e.valor_credito), 0)) > 0
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idAgencia", idAgencia);

        return jdbc.query(sql, params, (rs, rowNum) -> {

            DepreciacionPreviewDTO dto = new DepreciacionPreviewDTO();

            dto.setIdActivoFijo(rs.getLong("id_activo_fijo"));
            dto.setPlacaActivo(rs.getString("placa_activo"));
            dto.setNombreActivo(rs.getString("nombre_activo"));

            dto.setValorMensual(rs.getBigDecimal("valor_mensual"));
            dto.setDepreciacionAcumulada(rs.getBigDecimal("depreciacion_acumulada"));
            dto.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
            dto.setValorPeriodo(rs.getBigDecimal("valor_periodo"));

            dto.setIdCuentaGasto(rs.getObject("id_catalogo_cuenta_gasto", Long.class));
            dto.setIdCuentaDepreciacion(rs.getObject("id_catalogo_cuenta_depreciacion", Long.class));

            // ✅ BIGINT -> Long (evita error int8 -> Integer)
            dto.setIdDatosPersonalProveedor(rs.getObject("id_datos_personal_proveedor", Long.class));

            return dto;
        });
    }
}

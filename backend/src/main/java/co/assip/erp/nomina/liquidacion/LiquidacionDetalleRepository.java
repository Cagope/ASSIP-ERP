package co.assip.erp.nomina.liquidacion;

import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LiquidacionDetalleRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // 📑 INSERTAR DETALLE (BATCH)
    // =========================================================
    public void insertar(
            Integer idLiquidacion,
            List<LiquidacionDetalleDTO> detalles,
            Integer idUsuario
    ) {

        if (detalles == null || detalles.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO nomina.liquidacion_detalle (
                id_liquidacion,
                codigo_concepto,
                tipo,
                origen,
                cantidad,
                valor_unitario,
                valor_total,
                base_calculo,
                tipo_calculo,
                multiplicador,
                id_novedad_nomina,
                fk_seguridad_creacion,
                fecha_creacion
            ) VALUES (
                :idLiquidacion,
                :codigoConcepto,
                :tipo,
                :origen,
                :cantidad,
                :valorUnitario,
                :valorTotal,
                :baseCalculo,
                :tipoCalculo,
                :multiplicador,
                :idNovedadNomina,
                :idUsuario,
                NOW()
            )
        """;

        MapSqlParameterSource[] batch = detalles.stream()
                .map(d -> new MapSqlParameterSource()
                        .addValue("idLiquidacion", idLiquidacion)
                        .addValue("codigoConcepto", d.getCodigoConcepto())
                        .addValue("tipo", d.getTipo())
                        .addValue("origen", d.getOrigen())
                        .addValue("cantidad", d.getCantidad())
                        .addValue("valorUnitario", d.getValorUnitario())
                        .addValue("valorTotal", d.getValorTotal())
                        .addValue("baseCalculo", d.getBaseCalculo())
                        .addValue("tipoCalculo", d.getTipoCalculo())
                        .addValue("multiplicador", d.getMultiplicador())
                        .addValue("idNovedadNomina", d.getIdNovedadNomina())
                        .addValue("idUsuario", idUsuario)
                )
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate(sql, batch);
    }
}
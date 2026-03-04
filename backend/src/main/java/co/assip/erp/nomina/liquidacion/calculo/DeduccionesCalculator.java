package co.assip.erp.nomina.liquidacion.calculo;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.calculo.engine.*;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DeduccionesCalculator {

    private final NamedParameterJdbcTemplate jdbc;

    public List<LiquidacionDetalleDTO> calcular(
            Integer idPeriodoNomina,
            EmpleadoContratoDTO contrato,
            BigDecimal ibc
    ) {

        List<LiquidacionDetalleDTO> detalles = new ArrayList<>();

        // =========================
        // 1️⃣ DEDUCCIONES POR NOVEDADES (PRIORIDAD)
        // =========================
        Map<String, LiquidacionDetalleDTO> deduccionesPorNovedad =
                obtenerDeduccionesPorNovedadesMap(
                        idPeriodoNomina,
                        contrato.getIdContrato()
                );

        detalles.addAll(deduccionesPorNovedad.values());

        // =========================
        // CONTEXTO DE CÁLCULO
        // =========================
        ContextoCalculo ctx = new ContextoCalculo();
        ctx.put(BaseCalculo.IBC, ibc);

        // =========================
        // 2️⃣ DEDUCCIONES AUTOMÁTICAS (SI NO HAY NOVEDAD)
        // =========================
        detalles.addAll(
                obtenerDeduccionesAutomaticas(
                        ctx,
                        deduccionesPorNovedad.keySet()
                )
        );

        return detalles;
    }

    // =========================================================
    // 🔹 DEDUCCIONES AUTOMÁTICAS
    // =========================================================
    private List<LiquidacionDetalleDTO> obtenerDeduccionesAutomaticas(
            ContextoCalculo contexto,
            Set<String> codigosBloqueados
    ) {

        String sql = """
            SELECT
              c.codigo_concepto,
              c.tipo_calculo,
              c.base_calculo,
              c.multiplicador
            FROM nomina.conceptos_nomina c
            WHERE c.tipo_concepto = 'DEDUCCION'
              AND c.es_fijo = TRUE
              AND c.activo = TRUE
        """;

        return jdbc.query(sql, new MapSqlParameterSource(), (rs, row) -> {

            String codigo = rs.getString("codigo_concepto");

            // 🚫 Si hay novedad, no calcular automático
            if (codigosBloqueados.contains(codigo)) {
                return null;
            }

            TipoCalculo tipoCalculo =
                    TipoCalculo.fromDb(rs.getString("tipo_calculo"));

            BaseCalculo baseCalculo =
                    BaseCalculo.fromDb(rs.getString("base_calculo"));

            BigDecimal multiplicador =
                    rs.getBigDecimal("multiplicador");

            MotorCalculoConcepto.ResultadoCalculo r =
                    MotorCalculoConcepto.calcular(
                            tipoCalculo,
                            baseCalculo,
                            multiplicador,
                            BigDecimal.ONE,
                            contexto.asMap()
                    );

            return LiquidacionDetalleDTO.deduccionAutomatica(
                    codigo,
                    r.cantidad(),
                    r.valorUnitario(),
                    r.valorTotal(),
                    r.baseCalculo()
            );
        }).stream().filter(Objects::nonNull).toList();
    }

    // =========================================================
    // 🔹 DEDUCCIONES POR NOVEDADES (VALOR TOTAL REAL)
    // =========================================================
    private Map<String, LiquidacionDetalleDTO> obtenerDeduccionesPorNovedadesMap(
            Integer idPeriodoNomina,
            Integer idContrato
    ) {

        String sql = """
            SELECT
              n.id_novedad,
              n.codigo_concepto,
              n.cantidad,
              n.valor
            FROM nomina.novedades_nomina n
            JOIN nomina.conceptos_nomina c
              ON c.codigo_concepto = n.codigo_concepto
            WHERE n.id_periodo = :idPeriodo
              AND n.id_contrato = :idContrato
              AND n.estado = 'ABIERTO'
              AND c.tipo_concepto = 'DEDUCCION'
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodoNomina)
                .addValue("idContrato", idContrato);

        Map<String, LiquidacionDetalleDTO> map = new HashMap<>();

        jdbc.query(sql, params, rs -> {

            BigDecimal cantidad = rs.getBigDecimal("cantidad");
            if (cantidad == null || cantidad.signum() <= 0) {
                cantidad = BigDecimal.ONE;
            }

            BigDecimal total = rs.getBigDecimal("valor");
            if (total == null) {
                total = BigDecimal.ZERO;
            }

            BigDecimal valorUnitario =
                    total.divide(cantidad, 6, RoundingMode.HALF_UP);

            LiquidacionDetalleDTO dto =
                    LiquidacionDetalleDTO.deduccionNovedad(
                            rs.getString("codigo_concepto"),
                            cantidad,
                            valorUnitario,
                            total,
                            total,
                            rs.getInt("id_novedad")
                    );

            map.put(rs.getString("codigo_concepto"), dto);
        });

        return map;
    }
}
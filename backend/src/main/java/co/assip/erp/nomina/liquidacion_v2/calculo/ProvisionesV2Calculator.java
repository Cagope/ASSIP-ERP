package co.assip.erp.nomina.liquidacion_v2.calculo;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.calculo.engine.BaseCalculo;
import co.assip.erp.nomina.liquidacion.calculo.engine.ContextoCalculo;
import co.assip.erp.nomina.liquidacion.calculo.engine.MotorCalculoConcepto;
import co.assip.erp.nomina.liquidacion.calculo.engine.TipoCalculo;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.shared.math.MathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ProvisionesV2Calculator {

    private final NamedParameterJdbcTemplate jdbc;

    public List<LiquidacionDetalleDTO> calcular(
            Integer idPeriodoNomina,
            EmpleadoContratoDTO contrato,
            BigDecimal baseProvisiones
    ) {

        List<LiquidacionDetalleDTO> detalles = new ArrayList<>();

        Map<String, LiquidacionDetalleDTO> provisionesPorNovedad =
                obtenerProvisionesPorNovedadesMap(
                        idPeriodoNomina,
                        contrato.getIdContrato()
                );

        detalles.addAll(provisionesPorNovedad.values());

        if (baseProvisiones != null && baseProvisiones.compareTo(BigDecimal.ZERO) > 0) {

            ContextoCalculo ctx = new ContextoCalculo();
            ctx.put(BaseCalculo.SALARIO_BASE, baseProvisiones);
            ctx.put(BaseCalculo.IBC, baseProvisiones);

            detalles.addAll(
                    obtenerProvisionesAutomaticas(
                            contrato,
                            ctx,
                            provisionesPorNovedad.keySet()
                    )
            );
        }

        return detalles;
    }

    // =========================================================
    // 🔹 PROVISIONES AUTOMÁTICAS
    // =========================================================
    private List<LiquidacionDetalleDTO> obtenerProvisionesAutomaticas(
            EmpleadoContratoDTO contrato,
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
            WHERE c.tipo_concepto = 'PROVISION'
              AND c.es_fijo = TRUE
              AND c.activo = TRUE
        """;

        return jdbc.query(sql, new MapSqlParameterSource(), (rs, row) -> {

            String codigo = rs.getString("codigo_concepto");

            // 🚫 Si hay novedad manual/masiva, no calcular automático
            if (codigosBloqueados.contains(codigo)) {
                return null;
            }

            // 🚫 excluir por tipo de contrato
            if (excluirPorTipoContrato(contrato, codigo)) {
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

            return LiquidacionDetalleDTO.provisionAutomatica(
                    codigo,
                    r.cantidad(),
                    MathUtils.pesos(r.valorUnitario()),
                    MathUtils.pesos(r.valorTotal()),
                    MathUtils.pesos(r.baseCalculo())
            );
        }).stream().filter(Objects::nonNull).toList();
    }

    // =========================================================
    // 🔹 PROVISIONES POR NOVEDADES
    // =========================================================
    private Map<String, LiquidacionDetalleDTO> obtenerProvisionesPorNovedadesMap(
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
              AND c.tipo_concepto = 'PROVISION'
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
            total = MathUtils.pesos(total);

            BigDecimal valorUnitario =
                    MathUtils.pesos(
                            total.divide(cantidad, 8, RoundingMode.HALF_UP)
                    );

            LiquidacionDetalleDTO dto =
                    LiquidacionDetalleDTO.provisionNovedad(
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

    // =========================================================
    // 🔹 EXCLUSIONES POR TIPO DE CONTRATO
    // =========================================================
    private boolean excluirPorTipoContrato(
            EmpleadoContratoDTO contrato,
            String codigoConcepto
    ) {

        if (contrato == null || codigoConcepto == null) {
            return false;
        }

        if (codigoConcepto.equalsIgnoreCase("PROV_CESANTIAS")) {
            return Boolean.FALSE.equals(contrato.getAplicaCesantias());
        }

        if (codigoConcepto.equalsIgnoreCase("PROV_INT_CES")) {
            return Boolean.FALSE.equals(contrato.getAplicaCesantias());
        }

        if (codigoConcepto.equalsIgnoreCase("PRIMA_SEM")) {
            return Boolean.FALSE.equals(contrato.getAplicaPrima());
        }

        if (codigoConcepto.equalsIgnoreCase("PROV_VACACIONES")) {
            return Boolean.FALSE.equals(contrato.getAplicaVacaciones());
        }

        return false;
    }
}
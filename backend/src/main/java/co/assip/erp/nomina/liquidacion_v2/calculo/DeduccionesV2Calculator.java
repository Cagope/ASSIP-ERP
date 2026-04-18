package co.assip.erp.nomina.liquidacion_v2.calculo;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.calculo.engine.BaseCalculo;
import co.assip.erp.nomina.liquidacion.calculo.engine.ContextoCalculo;
import co.assip.erp.nomina.liquidacion.calculo.engine.MotorCalculoConcepto;
import co.assip.erp.nomina.liquidacion.calculo.engine.TipoCalculo;
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
public class DeduccionesV2Calculator {

    private final NamedParameterJdbcTemplate jdbc;

    public List<LiquidacionDetalleDTO> calcular(
            Integer idPeriodoNomina,
            EmpleadoContratoDTO contrato,
            BigDecimal ibc
    ) {

        List<LiquidacionDetalleDTO> detalles = new ArrayList<>();

        Map<String, LiquidacionDetalleDTO> deduccionesPorNovedad =
                obtenerDeduccionesPorNovedadesMap(
                        idPeriodoNomina,
                        contrato.getIdContrato()
                );

        detalles.addAll(deduccionesPorNovedad.values());

        if (ibc != null && ibc.compareTo(BigDecimal.ZERO) > 0) {

            ContextoCalculo ctx = new ContextoCalculo();
            ctx.put(BaseCalculo.IBC, ibc);

            detalles.addAll(
                    obtenerDeduccionesAutomaticas(
                            contrato,
                            ctx,
                            deduccionesPorNovedad.keySet()
                    )
            );
        }

        return detalles;
    }

    // =========================================================
    // 🔹 DEDUCCIONES AUTOMÁTICAS
    // =========================================================
    private List<LiquidacionDetalleDTO> obtenerDeduccionesAutomaticas(
            EmpleadoContratoDTO contrato,
            ContextoCalculo contexto,
            Set<String> codigosBloqueados
    ) {

        String sql = """
            SELECT
              c.codigo_concepto,
              c.tipo_calculo,
              c.base_calculo,
              c.multiplicador,
              c.smmlv_desde,
              c.smmlv_hasta
            FROM nomina.conceptos_nomina c
            WHERE c.tipo_concepto = 'DEDUCCION'
              AND c.es_fijo = TRUE
              AND c.activo = TRUE
        """;

        BigDecimal smmlvVigente = obtenerSmmlvVigente();

        return jdbc.query(sql, new MapSqlParameterSource(), (rs, row) -> {

            String codigo = rs.getString("codigo_concepto");

            // 🚫 Si hay novedad manual/masiva, no calcular automático
            if (codigosBloqueados.contains(codigo)) {
                return null;
            }

            // 🚫 Respetar reglas del tipo de contrato
            if ("SALUD_EMP".equalsIgnoreCase(codigo)
                    && Boolean.FALSE.equals(contrato.getAplicaSalud())) {
                return null;
            }

            if ("PENSION_EMP".equalsIgnoreCase(codigo)
                    && Boolean.FALSE.equals(contrato.getAplicaPension())) {
                return null;
            }

            BigDecimal smmlvDesde = rs.getBigDecimal("smmlv_desde");
            BigDecimal smmlvHasta = rs.getBigDecimal("smmlv_hasta");

            // 🚫 excluir por rango de salario base en SMMLV
            if (!aplicaPorRangoSmmlv(contrato, smmlvVigente, smmlvDesde, smmlvHasta)) {
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
    // 🔹 DEDUCCIONES POR NOVEDADES
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
            total = (total);

            BigDecimal valorUnitario =
                    total.divide(cantidad, 8, RoundingMode.HALF_UP);

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

    // =========================================================
    // 🔹 VALIDAR APLICACIÓN POR RANGO SMMLV
    // =========================================================
    private boolean aplicaPorRangoSmmlv(
            EmpleadoContratoDTO contrato,
            BigDecimal smmlvVigente,
            BigDecimal smmlvDesde,
            BigDecimal smmlvHasta
    ) {

        // Sin regla parametrizada → aplica siempre
        if (smmlvDesde == null && smmlvHasta == null) {
            return true;
        }

        if (contrato == null || contrato.getSalarioBase() == null) {
            return false;
        }

        if (smmlvVigente == null || smmlvVigente.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal salarioBase = contrato.getSalarioBase();
        BigDecimal minimo = smmlvDesde != null ? smmlvVigente.multiply(smmlvDesde) : null;
        BigDecimal maximo = smmlvHasta != null ? smmlvVigente.multiply(smmlvHasta) : null;

        if (minimo != null && salarioBase.compareTo(minimo) < 0) {
            return false;
        }

        if (maximo != null && salarioBase.compareTo(maximo) > 0) {
            return false;
        }

        return true;
    }

    // =========================================================
    // 🔹 OBTENER SMMLV VIGENTE
    // =========================================================
    private BigDecimal obtenerSmmlvVigente() {

        String sql = """
            SELECT smmlv
            FROM nomina.variables_vigencia
            WHERE activo = true
            ORDER BY fecha_inicial DESC
            LIMIT 1
        """;

        List<BigDecimal> lista = jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (rs, rowNum) -> rs.getBigDecimal("smmlv")
        );

        return lista.isEmpty() ? null : lista.get(0);
    }

}
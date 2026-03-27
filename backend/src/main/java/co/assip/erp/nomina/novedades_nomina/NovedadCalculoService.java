package co.assip.erp.nomina.novedades_nomina;

import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import co.assip.erp.shared.math.MathUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class NovedadCalculoService {

    private final NamedParameterJdbcTemplate jdbc;
    private final PeriodosNominaRepository periodosRepo;

    public BigDecimal calcularValor(
            Integer idContrato,
            String codigoConcepto,
            BigDecimal cantidad
    ) {

        if (idContrato == null || codigoConcepto == null) {
            return null;
        }

        if (cantidad == null) {
            cantidad = BigDecimal.ZERO;
        }

        // =====================================================
        // 🔹 PERÍODO OPERATIVO
        // =====================================================
        Integer idPeriodo = periodosRepo.obtenerPeriodoActivoId();
        if (idPeriodo == null) {
            return null;
        }

        // =====================================================
        // 🔹 CONTRATO
        // =====================================================
        String sqlContrato = """
            SELECT salario_base
            FROM nomina.empleado_contratos
            WHERE id_contrato = :idContrato
              AND activo = true
        """;

        BigDecimal salarioBase = jdbc.query(
                sqlContrato,
                new MapSqlParameterSource("idContrato", idContrato),
                rs -> rs.next() ? rs.getBigDecimal("salario_base") : null
        );

        if (salarioBase == null) {
            return null;
        }

        // =====================================================
        // 🔹 CONCEPTO
        // =====================================================
        String sqlConcepto = """
            SELECT tipo_calculo,
                   base_calculo,
                   multiplicador
            FROM nomina.conceptos_nomina
            WHERE codigo_concepto = :codigo
              AND activo = true
        """;

        Map<String, Object> concepto = jdbc.query(
                sqlConcepto,
                new MapSqlParameterSource("codigo", codigoConcepto),
                rs -> {
                    if (!rs.next()) return null;

                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("tipo", rs.getString("tipo_calculo"));
                    map.put("base", rs.getString("base_calculo"));
                    map.put("mult", rs.getBigDecimal("multiplicador"));
                    return map;
                }
        );

        if (concepto == null) {
            return null;
        }

        String tipo = (String) concepto.get("tipo");
        String baseCalculo = (String) concepto.get("base");
        BigDecimal multiplicador =
                concepto.get("mult") != null
                        ? (BigDecimal) concepto.get("mult")
                        : BigDecimal.ONE;

        // 🔥 MANUAL → NO CALCULA
        if ("MANUAL".equalsIgnoreCase(tipo)) {
            return null;
        }

        // =====================================================
        // 🔹 VARIABLES VIGENCIA
        // =====================================================
        String sqlVariables = """
            SELECT v.horas_mes, v.dias_mes
            FROM nomina.periodos_nomina p
            JOIN nomina.variables_vigencia v
              ON p.fecha_fin BETWEEN v.fecha_inicial AND v.fecha_final
             AND v.activo = true
            WHERE p.id_periodo = :idPeriodo
        """;

        Map<String, Object> vars = jdbc.query(
                sqlVariables,
                new MapSqlParameterSource("idPeriodo", idPeriodo),
                rs -> {
                    if (!rs.next()) return null;
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("horas", rs.getInt("horas_mes"));
                    m.put("dias", rs.getInt("dias_mes"));
                    return m;
                }
        );

        if (vars == null) {
            return null;
        }

        BigDecimal horasMes = BigDecimal.valueOf((Integer) vars.get("horas"));
        BigDecimal diasMes  = BigDecimal.valueOf((Integer) vars.get("dias"));

        // =====================================================
        // 🔹 BASE
        // =====================================================
        BigDecimal base = salarioBase;

        // (IBC se conecta aquí más adelante si lo deseas)
        if ("IBC".equalsIgnoreCase(baseCalculo)) {
            base = salarioBase;
        }

        // =====================================================
        // 🔹 CÁLCULO
        // =====================================================
        BigDecimal resultado;

        switch (tipo.toUpperCase()) {

            case "POR_DIAS" ->
                    resultado = MathUtils.pesos(
                            base
                                    .divide(diasMes, 8, RoundingMode.HALF_UP)
                                    .multiply(cantidad)
                                    .multiply(multiplicador)
                    );

            case "POR_HORAS" ->
                    resultado = MathUtils.pesos(
                            base
                                    .divide(horasMes, 8, RoundingMode.HALF_UP)
                                    .multiply(cantidad)
                                    .multiply(multiplicador)
                    );

            case "POR_PORCENTAJE" ->
                    resultado = MathUtils.pesos(
                            base.multiply(multiplicador)
                    );

            default ->
                    resultado = null;
        }

        return MathUtils.pesos(resultado);
    }
}
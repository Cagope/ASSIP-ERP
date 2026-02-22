package co.assip.erp.nomina.liquidacion.calculo;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class IbcCalculator {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Calcula el IBC de un contrato en un período.
     *
     * IBC =
     *  salario base proporcional por días
     *  + novedades DEVENGADO que afectan IBC
     */
    public BigDecimal calcular(
            Integer idPeriodoNomina,
            EmpleadoContratoDTO contrato
    ) {

        // =========================================
        // 1️⃣ DÍAS LABORADOS EN EL PERÍODO
        // =========================================
        Integer diasLaborados = obtenerDiasLaborados(
                idPeriodoNomina,
                contrato.getFechaInicio(),
                contrato.getFechaFin()
        );

        if (diasLaborados == null || diasLaborados <= 0) {
            return BigDecimal.ZERO;
        }

        // =========================================
        // 2️⃣ SALARIO BASE PROPORCIONAL
        // =========================================
        BigDecimal salarioBase = contrato.getSalarioBase();

        BigDecimal salarioProporcional = salarioBase
                .divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(diasLaborados));

        // =========================================
        // 3️⃣ NOVEDADES QUE AFECTAN IBC (VALOR TOTAL)
        // =========================================
        BigDecimal novedadesIbc = obtenerNovedadesQueAfectanIbc(
                idPeriodoNomina,
                contrato.getIdContrato()
        );

        // =========================================
        // 4️⃣ IBC FINAL
        // =========================================
        BigDecimal ibc = salarioProporcional.add(novedadesIbc);

        if (ibc.compareTo(BigDecimal.ZERO) < 0) {
            ibc = BigDecimal.ZERO;
        }

        return ibc.setScale(2, RoundingMode.HALF_UP);
    }

    // =========================================================
    // 🔎 DÍAS LABORADOS (PERÍODO vs CONTRATO)
    // =========================================================
    private Integer obtenerDiasLaborados(
            Integer idPeriodoNomina,
            LocalDate fechaInicioContrato,
            LocalDate fechaFinContrato
    ) {

        String sql = """
            SELECT
              GREATEST(
                0,
                LEAST(p.fecha_fin, COALESCE(:fechaFinContrato, p.fecha_fin))
                - GREATEST(p.fecha_inicio, :fechaInicioContrato)
                + 1
              )::int
            FROM nomina.periodos_nomina p
            WHERE p.id_periodo = :idPeriodo
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodoNomina)
                .addValue("fechaInicioContrato", fechaInicioContrato)
                .addValue("fechaFinContrato", fechaFinContrato);

        Integer dias = jdbc.queryForObject(sql, params, Integer.class);
        return dias != null ? dias : 0;
    }

    // =========================================================
// 🔎 NOVEDADES DEVENGADO QUE AFECTAN IBC
// =========================================================
    private BigDecimal obtenerNovedadesQueAfectanIbc(
            Integer idPeriodoNomina,
            Integer idContrato
    ) {

        String sql = """
        SELECT COALESCE(
            SUM(n.valor * COALESCE(n.cantidad, 1)),
            0
        )
        FROM nomina.novedades_nomina n
        JOIN nomina.conceptos_nomina c
          ON c.codigo_concepto = n.codigo_concepto
        WHERE n.id_periodo = :idPeriodo
          AND n.id_contrato = :idContrato
          AND n.estado = 'BORRADOR'
          AND c.tipo_concepto = 'DEVENGADO'
          AND c.afecta_ibc = TRUE
          AND c.codigo_concepto <> 'BASICO'   -- 🔒 CLAVE
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idPeriodo", idPeriodoNomina)
                .addValue("idContrato", idContrato);

        BigDecimal v = jdbc.queryForObject(sql, params, BigDecimal.class);
        return v != null ? v : BigDecimal.ZERO;
    }
}
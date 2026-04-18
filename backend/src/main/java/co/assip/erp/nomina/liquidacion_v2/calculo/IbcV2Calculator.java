package co.assip.erp.nomina.liquidacion_v2.calculo;

import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.shared.math.MathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class IbcV2Calculator {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Calcula el IBC basado en los devengados ya calculados.
     *
     * Regla:
     * - Solo suma conceptos DEVENGADO
     * - Solo suma conceptos parametrizados en nomina.conceptos_nomina con afecta_ibc = TRUE
     */
    public BigDecimal calcular(List<LiquidacionDetalleDTO> devengados) {

        if (devengados == null || devengados.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<String> codigosDevengados = new HashSet<>();

        for (LiquidacionDetalleDTO d : devengados) {
            if (d == null) continue;
            if (!"DEVENGADO".equalsIgnoreCase(d.getTipo())) continue;
            if (d.getCodigoConcepto() == null || d.getCodigoConcepto().isBlank()) continue;

            codigosDevengados.add(d.getCodigoConcepto());
        }

        if (codigosDevengados.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<String> codigosQueAfectanIbc = obtenerCodigosQueAfectanIbc(codigosDevengados);

        BigDecimal ibc = BigDecimal.ZERO;

        for (LiquidacionDetalleDTO d : devengados) {

            if (d == null) continue;
            if (!"DEVENGADO".equalsIgnoreCase(d.getTipo())) continue;
            if (d.getCodigoConcepto() == null) continue;

            if (!codigosQueAfectanIbc.contains(d.getCodigoConcepto())) {
                continue;
            }

            BigDecimal valor = d.getValorTotal() != null
                    ? d.getValorTotal()
                    : BigDecimal.ZERO;

            ibc = ibc.add(valor);
        }

        if (ibc.compareTo(BigDecimal.ZERO) < 0) {
            ibc = BigDecimal.ZERO;
        }

        return MathUtils.pesos(ibc);
    }

    private Set<String> obtenerCodigosQueAfectanIbc(Set<String> codigos) {

        String sql = """
            SELECT c.codigo_concepto
            FROM nomina.conceptos_nomina c
            WHERE c.codigo_concepto IN (:codigos)
              AND c.tipo_concepto = 'DEVENGADO'
              AND c.afecta_ibc = TRUE
        """;

        List<String> lista = jdbc.query(
                sql,
                new MapSqlParameterSource("codigos", codigos),
                (rs, rowNum) -> rs.getString("codigo_concepto")
        );

        return new HashSet<>(lista);
    }
}
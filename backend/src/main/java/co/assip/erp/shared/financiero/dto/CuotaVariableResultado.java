package co.assip.erp.shared.financiero.dto;

import java.math.BigDecimal;

public record CuotaVariableResultado(
        BigDecimal valorCuotaRegular,
        BigDecimal valorPrimeraCuota
) {
}
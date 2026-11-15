package co.assip.erp.contabilidad.cierres.diario.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Consolida el control de cierres diarios de una agencia
 * para un mes completo, incluyendo días sin movimientos.
 */
public record CierreDiarioMensualDTO(
        Integer idAgencia,
        int ano,
        int mes,
        String estadoPeriodoMensual,
        int diasAbiertos,
        int diasCerrados,
        int diasConDiferencias,
        int diasSinMovimientos,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia,
        List<CierreDiarioDiaDTO> dias
) {
}
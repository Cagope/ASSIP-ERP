package co.assip.erp.contabilidad.cierres.diario.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Contiene el estado y los resultados de la consulta
 * del cierre diario de una agencia, incluyendo todos
 * los tipos de comprobantes.
 */
public record CierreDiarioResumenDTO(
        Integer idAgencia,
        LocalDate fecha,
        String estadoPeriodoMensual,
        String estadoDia,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia,
        Long cantidadComprobantes,
        Long cantidadMovimientos,
        List<CierreDiarioDetalleCuentaDTO> detalleCuentas
) {
}
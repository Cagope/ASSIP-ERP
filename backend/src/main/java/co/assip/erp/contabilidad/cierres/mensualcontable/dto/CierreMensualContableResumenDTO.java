package co.assip.erp.contabilidad.cierres.mensualcontable.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resume la consulta previa del cierre mensual.
 * Presenta el estado del período, las validaciones y
 * los totales contables sin modificar registros.
 */
public record CierreMensualContableResumenDTO(
        Integer idAgencia,
        String nombreAgencia,
        Integer ano,
        Integer mes,
        String estadoPeriodo,
        boolean puedeCerrar,
        String mensajeValidacion,
        int cantidadDiasMes,
        int cantidadDiasCerrados,
        int cantidadDiasPendientes,
        List<CierreMensualContableDiaPendienteDTO> diasPendientes,
        int cantidadCuentas,
        int cantidadMovimientos,
        BigDecimal totalDebitoAnterior,
        BigDecimal totalCreditoAnterior,
        BigDecimal totalDebitoMes,
        BigDecimal totalCreditoMes,
        BigDecimal totalDebitoAcumulado,
        BigDecimal totalCreditoAcumulado,
        BigDecimal diferenciaMes
) {
}
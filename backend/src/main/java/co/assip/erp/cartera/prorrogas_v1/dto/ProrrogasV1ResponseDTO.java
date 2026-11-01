package co.assip.erp.cartera.prorrogas_v1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado de la ejecución de una prórroga.
 * Informa las fechas anteriores y nuevas, el recaudo y el registro generado.
 */
public record ProrrogasV1ResponseDTO(
        Long idCreditoProrroga,
        Integer idCarteraCredito,
        Integer numeroProrroga,
        Integer mesesProrroga,
        Integer diasProrroga,
        LocalDate fechaProrroga,
        LocalDate proximaFechaCapitalAnterior,
        LocalDate proximaFechaCapitalNueva,
        LocalDate proximaFechaInteresAnterior,
        LocalDate proximaFechaInteresNueva,
        LocalDate fechaVencimientoAnterior,
        LocalDate fechaVencimientoNueva,
        BigDecimal valorInteresCorriente,
        BigDecimal valorInteresMora,
        BigDecimal valorSeguro,
        BigDecimal valorAportes,
        BigDecimal valorFondoGarantia,
        BigDecimal valorOtrosConceptos,
        BigDecimal valorTotalPagado,
        String tipoComprobante,
        String numeroComprobante,
        LocalDate fechaComprobante,
        String estadoProrroga,
        String mensaje
) {
}
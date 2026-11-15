
package co.assip.erp.cartera.prorrogas_v1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado de la ejecución de una prórroga.
 * Informa las fechas anteriores y nuevas, los conceptos
 * recaudados y el comprobante contable generado.
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
        BigDecimal valorCapital,
        BigDecimal valorInteresCorriente,
        BigDecimal valorInteresMora,
        BigDecimal valorSeguro,
        BigDecimal valorAlivios,
        BigDecimal valorCostasJudiciales,
        BigDecimal valorTotalPagado,
        String tipoComprobante,
        String numeroComprobante,
        LocalDate fechaComprobante,
        String estadoProrroga,
        String mensaje
) {
}

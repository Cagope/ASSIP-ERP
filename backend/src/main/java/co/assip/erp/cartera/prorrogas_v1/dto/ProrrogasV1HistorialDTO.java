package co.assip.erp.cartera.prorrogas_v1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa una prórroga registrada en el historial del crédito.
 * Permite consultar fechas, valores recaudados, comprobante y estado.
 */
public record ProrrogasV1HistorialDTO(
        Long idCreditoProrroga,
        Integer idCarteraCredito,
        Integer numeroProrroga,
        LocalDate fechaProrroga,
        Integer diasProrroga,
        Integer mesesProrroga,
        LocalDate proximaFechaCapitalAnterior,
        LocalDate proximaFechaInteresAnterior,
        LocalDate fechaVencimientoAnterior,
        Integer plazoAnterior,
        LocalDate proximaFechaCapitalNueva,
        LocalDate proximaFechaInteresNueva,
        LocalDate fechaVencimientoNueva,
        Integer plazoNuevo,
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
        Integer idAgencia,
        String estadoProrroga,
        LocalDateTime fechaAplicacion,
        String observacion
) {
}
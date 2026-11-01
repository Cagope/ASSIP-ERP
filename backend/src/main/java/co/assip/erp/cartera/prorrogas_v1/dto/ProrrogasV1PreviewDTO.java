package co.assip.erp.cartera.prorrogas_v1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Información previa para simular una prórroga.
 * Presenta el estado del crédito, las fechas y los conceptos
 * liquidados antes de confirmar la operación.
 */
public record ProrrogasV1PreviewDTO(
        Integer idCarteraCredito,
        Integer idAgencia,
        String numeroPagare,
        String documento,
        String nombreCompleto,
        String estadoCredito,
        BigDecimal saldoCapital,
        BigDecimal valorCuota,
        Integer numeroCuota,
        Integer diasMora,
        LocalDate fechaConsulta,
        LocalDate proximaFechaCapital,
        LocalDate proximaFechaInteres,
        LocalDate fechaVencimiento,
        Integer mesesProrroga,
        LocalDate proximaFechaCapitalNueva,
        LocalDate proximaFechaInteresNueva,
        LocalDate fechaVencimientoNueva,
        BigDecimal valorInteresCorriente,
        BigDecimal valorInteresMora,
        BigDecimal valorCapital,
        BigDecimal valorSeguro,
        BigDecimal valorAportes,
        BigDecimal valorFondoGarantia,
        BigDecimal valorOtrosConceptos,
        BigDecimal valorTotalRecaudo,
        boolean tieneProrrogaActiva,
        boolean permiteProrroga,
        String mensajeValidacion
) {
}
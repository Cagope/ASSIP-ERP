package co.assip.erp.cartera.analisis.rollforward.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RollForwardDetalleDTO(
        LocalDate fechaCorteAnterior, LocalDate fechaCorte,
        Long idCierreCarteraAnterior, Long idCierreCarteraActual,
        Long idCierreCarteraCreditoAnterior, Long idCierreCarteraCreditoActual,
        Long idCarteraCredito, Long idAgencia,
        Long idLineaCredito, String codigoLineaCredito, String nombreLineaCredito,
        String pagareCartera,
        Long idDatosPersonal, String tipoDocumento, String documento, String nombreCompleto,
        String codigoClasificacionCredito, String descripcionClasificacionCredito,
        String codigoDestinoEconomico, String descripcionDestinoEconomico,
        BigDecimal valorInicialCredito, BigDecimal valorDesembolsado,
        LocalDate fechaDesembolso, LocalDate fechaFinal,
        Integer plazo, String codigoFormaPago, String codigoTipoCuota,
        Integer amortizacionCapital, BigDecimal valorCuota,
        LocalDate primeraAparicion, LocalDate ultimaAparicion,
        Boolean existeCorteAnterior, Boolean existeCorteActual,
        BigDecimal saldoAnterior, BigDecimal saldoActual, BigDecimal variacionSaldo,
        String tipoMovimiento, Integer diasAnticipacion, String rangoSalida,
        Integer diasMoraAnterior, Integer diasMoraActual,
        String edadContableAnterior, String edadContableActual,
        BigDecimal deterioroCapitalAnterior, BigDecimal deterioroCapitalActual,
        BigDecimal deterioroInteresesAnterior, BigDecimal deterioroInteresesActual,
        BigDecimal deterioroOtrosAnterior, BigDecimal deterioroOtrosActual,
        BigDecimal deterioroTotalAnterior, BigDecimal deterioroTotalActual,
        BigDecimal valorNuevos, BigDecimal valorReingresos,
        BigDecimal valorAumentosSaldo, BigDecimal valorReduccionesSaldo,
        BigDecimal valorPrepagos, BigDecimal valorCancelacionesNormales,
        BigDecimal valorCancelacionesPostVencimiento, BigDecimal valorAusenciasTemporales,
        BigDecimal valorOtrasSalidas, BigDecimal valorMovimientoNeto
) {
}

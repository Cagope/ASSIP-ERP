package co.assip.erp.cartera.analisis.garantiascobertura.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record GarantiasCoberturaResumenDTO(Integer idCierreCartera, LocalDate fechaCorte, Long cantidadBienes, BigDecimal valorTotalBienes, Long bienesSuficientes, Long bienesInsuficientes, BigDecimal margenTotalBienes, BigDecimal deficitTotalBienes, Long cantidadCreditosConBien, Long creditosConMultiplesBienes, BigDecimal saldoCreditosConBien, BigDecimal valorGarantiasAsignadas, BigDecimal coberturaEfectivaCreditos, BigDecimal exposicionNoCubierta, Long creditosCoberturaSuficiente, Long creditosCoberturaInsuficiente, BigDecimal porcentajeCoberturaEfectiva, BigDecimal porcentajeExposicionNoCubierta) {}

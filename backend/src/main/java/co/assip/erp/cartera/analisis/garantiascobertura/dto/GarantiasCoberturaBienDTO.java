package co.assip.erp.cartera.analisis.garantiascobertura.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record GarantiasCoberturaBienDTO(Integer idCierreCartera, LocalDate fechaCorte, Long idBien, String tipoBien, String descripcionBien, String identificacionBien, BigDecimal valorBienFechaCorte, Long cantidadCreditosBien, BigDecimal saldoTotalCreditosBien, BigDecimal valorGarantiaDistribuida, BigDecimal coberturaEfectivaBien, BigDecimal porcentajeCoberturaBien, BigDecimal margenCobertura, BigDecimal deficitCobertura, String estadoCobertura) {}

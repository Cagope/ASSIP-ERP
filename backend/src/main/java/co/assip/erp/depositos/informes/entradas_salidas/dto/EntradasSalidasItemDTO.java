package co.assip.erp.depositos.informes.entradas_salidas.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class EntradasSalidasItemDTO {

    private LocalDate fechaMovimiento;

    // 🆕 Forma ahorro
    private String codigoForma;
    private String nombreForma;

    // 🆕 Detalle movimiento
    private String codigoCuenta;
    private String documento;
    private String nombreCompleto;

    private String codigoMovimiento;
    private String nombreMovimiento;

    private String numeroMovimiento;

    private BigDecimal debito;
    private BigDecimal credito;

    private Integer cantidadMovimientos;

    private BigDecimal entradas;
    private BigDecimal salidas;
    private BigDecimal neto;

}
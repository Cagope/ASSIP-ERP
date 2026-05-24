package co.assip.erp.depositos.informes.saldos_menores.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class SaldosMenoresItemDTO {

    private Integer idAgencia;
    private String nombreAgencia;

    private String codigoForma;
    private String nombreForma;

    private String codigoCuenta;
    private String documento;
    private String nombreCompleto;

    private LocalDate fechaApertura;
    private String estadoCuenta;

    private BigDecimal saldoCorte;

}
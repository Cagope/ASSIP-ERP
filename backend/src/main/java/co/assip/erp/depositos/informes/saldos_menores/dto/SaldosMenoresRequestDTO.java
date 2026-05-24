package co.assip.erp.depositos.informes.saldos_menores.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SaldosMenoresRequestDTO {

    private LocalDate fechaCorte;
    private Integer idAgencia;
    private String codigoForma;
    private BigDecimal valorMaximo;

}
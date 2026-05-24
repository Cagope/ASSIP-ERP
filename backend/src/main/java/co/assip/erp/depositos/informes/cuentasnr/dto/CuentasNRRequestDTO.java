package co.assip.erp.depositos.informes.cuentasnr.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CuentasNRRequestDTO {

    private Integer idAgencia;

    private String forma;

    private LocalDate fechaInicial;
    private LocalDate fechaFinal;

    // NUEVAS o RETIRADAS
    private String tipoInforme;

}
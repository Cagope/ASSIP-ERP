package co.assip.erp.depositos.informes.cumpleanios_asociados.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CumpleaniosAsociadosRequestDTO {

    private LocalDate fechaInicial;
    private LocalDate fechaFinal;

    private Integer idAgencia;

}
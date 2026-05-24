package co.assip.erp.depositos.informes.antiguedad_asociados.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AntiguedadAsociadosRequestDTO {

    private LocalDate fechaCorte;
    private Integer idAgencia;
    private Integer limitePantalla;

}
package co.assip.erp.depositos.informes.promedios.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PromediosRequestDTO {

    private LocalDate fechaCorte;

    private Integer idAgencia;

    private String codigoForma;

    private Integer limitePantalla;

}
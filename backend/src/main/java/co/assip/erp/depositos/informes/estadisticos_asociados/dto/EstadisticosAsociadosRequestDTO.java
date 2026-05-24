package co.assip.erp.depositos.informes.estadisticos_asociados.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EstadisticosAsociadosRequestDTO {

    private LocalDate fechaCorte;

    private Integer idAgencia;

    private String codigoForma;

}
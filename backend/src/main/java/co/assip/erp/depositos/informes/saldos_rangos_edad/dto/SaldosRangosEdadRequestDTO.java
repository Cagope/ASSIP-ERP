package co.assip.erp.depositos.informes.saldos_rangos_edad.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SaldosRangosEdadRequestDTO {

    private LocalDate fechaCorte;
    private Integer idAgencia;
    private List<SaldosRangosEdadRangoDTO> rangos;

}
package co.assip.erp.depositos.informes.saldos_rangos_edad.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaldosRangosEdadRangoDTO {

    private String nombreRango;
    private Integer edadInicial;
    private Integer edadFinal;

}
package co.assip.erp.depositos.interesmensual_sm.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InteresMensualSMEntradaDTO {

    private Integer agenciaId;
    private Integer formaId;

    private LocalDate fechaProceso;
    private LocalDate fechaLiquidacion;  // fin del mes

}

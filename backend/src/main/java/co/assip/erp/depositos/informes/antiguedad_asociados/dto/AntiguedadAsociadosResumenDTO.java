package co.assip.erp.depositos.informes.antiguedad_asociados.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AntiguedadAsociadosResumenDTO {

    private String rangoAntiguedad;
    private Integer cantidadAsociados;
    private BigDecimal saldoTotalAportes;
    private BigDecimal saldoPromedioAportes;
    private BigDecimal edadPromedio;

}
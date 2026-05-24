package co.assip.erp.cdat.informes.estadisticos_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EstadisticosCdatResumenDTO {

    private Integer totalCdats;

    private BigDecimal valorTotalCaptado;

    private BigDecimal promedioTasa;

    private BigDecimal promedioPlazo;

    private Integer vencen30Dias;
}
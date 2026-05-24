package co.assip.erp.cdat.informes.estadisticos_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EstadisticosCdatTasaDTO {

    private String tasa;

    private Integer cantidad;

    private BigDecimal valorTotal;

    private BigDecimal promedioPlazo;

    private BigDecimal participacion;
}
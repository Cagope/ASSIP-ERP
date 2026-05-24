package co.assip.erp.depositos.informes.estadisticos_asociados.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EstadisticosAsociadosItemDTO {

    private String grupo;
    private String categoria;

    private Integer cantidad;

    private BigDecimal saldoTotal;

    private BigDecimal porcentaje;

}
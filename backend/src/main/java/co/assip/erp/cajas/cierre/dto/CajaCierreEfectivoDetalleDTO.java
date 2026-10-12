package co.assip.erp.cajas.cierre.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CajaCierreEfectivoDetalleDTO {

    private String tipoDetalle;

    private BigDecimal denominacion;

    private Integer cantidad;

    private BigDecimal valorTotal;
}
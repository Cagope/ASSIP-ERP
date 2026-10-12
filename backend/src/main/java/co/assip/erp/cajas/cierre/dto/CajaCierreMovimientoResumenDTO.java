package co.assip.erp.cajas.cierre.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CajaCierreMovimientoResumenDTO {

    private Long idDepartamentoOperativo;
    private String codigoDepartamento;
    private String nombreDepartamento;

    private String naturaleza;

    private Integer cantidadMovimientos;

    private BigDecimal valorEfectivo;
    private BigDecimal valorCheques;
    private BigDecimal valorOtros;

    private BigDecimal valorTotal;
}
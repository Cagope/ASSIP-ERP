package co.assip.erp.cajas.cierre.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CajaCierreVentanillaDTO {

    private BigDecimal cajaAVentanilla;
    private BigDecimal ventanillaACaja;

    private BigDecimal entradasOperacionesEfectivo;
    private BigDecimal salidasOperacionesEfectivo;

    private BigDecimal saldoVentanilla;
    private BigDecimal saldoCajaInterna;
}

package co.assip.erp.depositos.informes.movimientos_diarios.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MovimientosDiariosResumenDTO {

    private Integer totalMovimientos;

    private BigDecimal totalDebitos;
    private BigDecimal totalCreditos;

}
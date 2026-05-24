package co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ResumenTipoMovimientoResumenDTO {

    private Integer totalTipos;
    private Integer totalMovimientos;

    private BigDecimal totalDebitos;
    private BigDecimal totalCreditos;
    private BigDecimal totalNeto;

}
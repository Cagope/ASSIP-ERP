package co.assip.erp.cdat.liquidacion_diaria.dto;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CdatLiquidacionDiariaPreviewDTO {

    private Integer totalCdats;

    private BigDecimal totalInteres;

    private BigDecimal totalRetencion;

    private BigDecimal totalNeto;

    private BigDecimal totalTrasladadoDepositos;

    private BigDecimal totalNoTrasladado;

    private BigDecimal totalDebito;

    private BigDecimal totalCredito;

    private BigDecimal diferenciaContable;

    private Boolean cuadrado;

    private List<CdatLiquidacionDiariaItemDTO> items;

    private List<MovimientoContablePreviewDTO> movimientosContables;
}
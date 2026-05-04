package co.assip.erp.cdat.cdats.contabilizacion.dto;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CdatAperturaPreviewDTO {

    private List<MovimientoContablePreviewDTO> movimientos;

    private BigDecimal totalDebito;
    private BigDecimal totalCredito;
    private BigDecimal diferencia;

    private Boolean cuadrado;
}
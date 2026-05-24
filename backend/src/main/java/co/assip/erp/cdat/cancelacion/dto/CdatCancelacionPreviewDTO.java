package co.assip.erp.cdat.cancelacion.dto;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CdatCancelacionPreviewDTO {

    private CdatCancelacionItemDTO cdat;

    private BigDecimal valorCapital;
    private BigDecimal valorInteresCausado;
    private BigDecimal valorInteresCorriente;
    private BigDecimal valorRetencion;

    private BigDecimal valorDisponible;
    private BigDecimal valorRenovacion;
    private BigDecimal valorDiferencia;

    private String tipoOperacionDiferencia; // ENTRADA / SALIDA / NINGUNA

    private List<MovimientoContablePreviewDTO> movimientosContables;

    private BigDecimal totalDebito;
    private BigDecimal totalCredito;
    private BigDecimal diferenciaContable;

    private Boolean cuadrado;
}
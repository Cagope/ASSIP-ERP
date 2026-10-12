package co.assip.erp.tesoreria.consignaciones_bancarias.dto;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContablePreviewDTO;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ConsignacionBancariaPreviewResponse {

    // =========================================================
    // MOVIMIENTOS CONTABLES
    // =========================================================

    private List<MovimientoContablePreviewDTO> movimientos;

    // =========================================================
    // TOTALES CONTABLES
    // =========================================================

    private BigDecimal totalDebito;
    private BigDecimal totalCredito;
    private BigDecimal diferencia;

    private Boolean cuadrado;

    // =========================================================
    // INFORMACIÓN DE LA CONSIGNACIÓN
    // =========================================================

    private BigDecimal valorCheques;
    private BigDecimal valorEfectivo;
    private BigDecimal valorTotal;

    private String conceptoComprobante;
}
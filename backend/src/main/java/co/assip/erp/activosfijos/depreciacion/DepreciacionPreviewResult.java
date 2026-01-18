package co.assip.erp.activosfijos.depreciacion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado del preview de depreciación.
 */
public class DepreciacionPreviewResult {

    private List<DepreciacionPreviewDTO> detalle;
    private BigDecimal totalDebito;
    private BigDecimal totalCredito;

    public List<DepreciacionPreviewDTO> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DepreciacionPreviewDTO> detalle) {
        this.detalle = detalle;
    }

    public BigDecimal getTotalDebito() {
        return totalDebito;
    }

    public void setTotalDebito(BigDecimal totalDebito) {
        this.totalDebito = totalDebito;
    }

    public BigDecimal getTotalCredito() {
        return totalCredito;
    }

    public void setTotalCredito(BigDecimal totalCredito) {
        this.totalCredito = totalCredito;
    }
}

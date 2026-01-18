package co.assip.erp.activosfijos.depreciacion;

import java.math.BigDecimal;

/**
 * Resultado de la ejecución del proceso de depreciación.
 */
public class DepreciacionEjecucionResult {

    private int activosProcesados;
    private BigDecimal totalDebito;
    private BigDecimal totalCredito;

    public int getActivosProcesados() {
        return activosProcesados;
    }

    public void setActivosProcesados(int activosProcesados) {
        this.activosProcesados = activosProcesados;
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

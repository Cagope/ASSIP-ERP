package co.assip.erp.contabilidad.registro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa UNA línea contable (auxiliar contable).
 * Se usa para cualquier módulo.
 *
 * Nota:
 * - documentoSoporte SOLO se usa si la cuenta contable exige control_entrada_salida = true.
 */
public class MovimientoContableDTO {

    // =========================
    // IDENTIDAD CONTABLE
    // =========================
    private Integer idCatalogoCuenta;
    private Integer idAgencia;
    private Integer idDatosPersonal;

    private LocalDate fechaAuxiliar;
    private String tipoComprobante;
    private String numeroComprobante;

    // Texto contable (automático estandarizado o manual libre validado)
    private String detalleMovimiento;

    // =========================
    // VALORES
    // =========================
    private BigDecimal valorDebito = BigDecimal.ZERO;
    private BigDecimal valorCredito = BigDecimal.ZERO;
    private BigDecimal valorBase = BigDecimal.ZERO;

    // =========================
    // SOPORTE (OPCIONAL)
    // =========================
    private String documentoSoporte;   // ej: "CHEQUE 12345", "TRANSFERENCIA 8899"

    // =========================
    // GETTERS / SETTERS
    // =========================
    public Integer getIdCatalogoCuenta() {
        return idCatalogoCuenta;
    }

    public void setIdCatalogoCuenta(Integer idCatalogoCuenta) {
        this.idCatalogoCuenta = idCatalogoCuenta;
    }

    public Integer getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Integer idAgencia) {
        this.idAgencia = idAgencia;
    }

    public Integer getIdDatosPersonal() {
        return idDatosPersonal;
    }

    public void setIdDatosPersonal(Integer idDatosPersonal) {
        this.idDatosPersonal = idDatosPersonal;
    }

    public LocalDate getFechaAuxiliar() {
        return fechaAuxiliar;
    }

    public void setFechaAuxiliar(LocalDate fechaAuxiliar) {
        this.fechaAuxiliar = fechaAuxiliar;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public String getDetalleMovimiento() {
        return detalleMovimiento;
    }

    public void setDetalleMovimiento(String detalleMovimiento) {
        this.detalleMovimiento = detalleMovimiento;
    }

    public BigDecimal getValorDebito() {
        return valorDebito;
    }

    public void setValorDebito(BigDecimal valorDebito) {
        this.valorDebito = valorDebito;
    }

    public BigDecimal getValorCredito() {
        return valorCredito;
    }

    public void setValorCredito(BigDecimal valorCredito) {
        this.valorCredito = valorCredito;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }

    public void setValorBase(BigDecimal valorBase) {
        this.valorBase = valorBase;
    }

    public String getDocumentoSoporte() {
        return documentoSoporte;
    }

    public void setDocumentoSoporte(String documentoSoporte) {
        this.documentoSoporte = documentoSoporte;
    }
}

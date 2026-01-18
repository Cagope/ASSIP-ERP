package co.assip.erp.activosfijos.depreciacion;

import java.math.BigDecimal;

/**
 * DTO de salida para el preview de depreciación.
 * Representa una fila por activo.
 */
public class DepreciacionPreviewDTO {

    // =========================
    // IDENTIFICACIÓN DEL ACTIVO
    // =========================
    private Long idActivoFijo;
    private String placaActivo;
    private String nombreActivo;

    // =========================
    // TERCERO CONTABLE (PROVEEDOR)
    // =========================
    private Long idDatosPersonalProveedor; // ✅ BIGINT -> Long

    // =========================
    // VALORES DE DEPRECIACIÓN
    // =========================
    private BigDecimal valorMensual;
    private BigDecimal depreciacionAcumulada;
    private BigDecimal saldoPendiente;
    private BigDecimal valorPeriodo;

    // =========================
    // CUENTAS CONTABLES
    // =========================
    private Long idCuentaGasto;
    private Long idCuentaDepreciacion;

    // =========================
    // GETTERS / SETTERS
    // =========================

    public Long getIdActivoFijo() {
        return idActivoFijo;
    }

    public void setIdActivoFijo(Long idActivoFijo) {
        this.idActivoFijo = idActivoFijo;
    }

    public String getPlacaActivo() {
        return placaActivo;
    }

    public void setPlacaActivo(String placaActivo) {
        this.placaActivo = placaActivo;
    }

    public String getNombreActivo() {
        return nombreActivo;
    }

    public void setNombreActivo(String nombreActivo) {
        this.nombreActivo = nombreActivo;
    }

    public Long getIdDatosPersonalProveedor() {
        return idDatosPersonalProveedor;
    }

    public void setIdDatosPersonalProveedor(Long idDatosPersonalProveedor) {
        this.idDatosPersonalProveedor = idDatosPersonalProveedor;
    }

    public BigDecimal getValorMensual() {
        return valorMensual;
    }

    public void setValorMensual(BigDecimal valorMensual) {
        this.valorMensual = valorMensual;
    }

    public BigDecimal getDepreciacionAcumulada() {
        return depreciacionAcumulada;
    }

    public void setDepreciacionAcumulada(BigDecimal depreciacionAcumulada) {
        this.depreciacionAcumulada = depreciacionAcumulada;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public BigDecimal getValorPeriodo() {
        return valorPeriodo;
    }

    public void setValorPeriodo(BigDecimal valorPeriodo) {
        this.valorPeriodo = valorPeriodo;
    }

    public Long getIdCuentaGasto() {
        return idCuentaGasto;
    }

    public void setIdCuentaGasto(Long idCuentaGasto) {
        this.idCuentaGasto = idCuentaGasto;
    }

    public Long getIdCuentaDepreciacion() {
        return idCuentaDepreciacion;
    }

    public void setIdCuentaDepreciacion(Long idCuentaDepreciacion) {
        this.idCuentaDepreciacion = idCuentaDepreciacion;
    }
}

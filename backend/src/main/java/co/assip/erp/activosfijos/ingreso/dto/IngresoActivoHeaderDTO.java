package co.assip.erp.activosfijos.ingreso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class IngresoActivoHeaderDTO {

    // =========================================================
    // AGENCIA (OBLIGATORIA)
    // =========================================================
    @NotNull
    private Long idAgencia;

    // =========================================================
    // DATOS CONTABLES
    // =========================================================
    @NotNull
    private LocalDate fechaInclusion;

    @NotBlank
    private String tipoComprobante;

    /**
     * 10 dígitos solo números (se normaliza/pad en service).
     * Puede venir vacío para autogenerar consecutivo.
     */
    private String numeroComprobante;

    @NotBlank
    @Size(max = 100)
    private String detalle;

    // =========================================================
    // RELACIONES
    // =========================================================
    @NotNull
    private Long idProveedor;

    @NotNull
    private Long idCuentaFactura;

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================
    public Long getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Long idAgencia) {
        this.idAgencia = idAgencia;
    }

    public LocalDate getFechaInclusion() {
        return fechaInclusion;
    }

    public void setFechaInclusion(LocalDate fechaInclusion) {
        this.fechaInclusion = fechaInclusion;
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

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Long getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Long idProveedor) {
        this.idProveedor = idProveedor;
    }

    public Long getIdCuentaFactura() {
        return idCuentaFactura;
    }

    public void setIdCuentaFactura(Long idCuentaFactura) {
        this.idCuentaFactura = idCuentaFactura;
    }
}

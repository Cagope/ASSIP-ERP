package co.assip.erp.contabilidad.tipos_comprobantes;

import java.time.LocalDateTime;

public class TipoComprobante {

    private String tipoComprobante;
    private Integer idAgencia;
    private String nombreTipoComprobante;
    private Integer cscComprobante;
    private Boolean comprobanteActivo;

    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;
    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;

    // ============================
    // GETTERS & SETTERS
    // ============================

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public Integer getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Integer idAgencia) {
        this.idAgencia = idAgencia;
    }

    public String getNombreTipoComprobante() {
        return nombreTipoComprobante;
    }

    public void setNombreTipoComprobante(String nombreTipoComprobante) {
        this.nombreTipoComprobante = nombreTipoComprobante;
    }

    public Integer getCscComprobante() {
        return cscComprobante;
    }

    public void setCscComprobante(Integer cscComprobante) {
        this.cscComprobante = cscComprobante;
    }

    public Boolean getComprobanteActivo() {
        return comprobanteActivo;
    }

    public void setComprobanteActivo(Boolean comprobanteActivo) {
        this.comprobanteActivo = comprobanteActivo;
    }

    public Integer getFkSeguridadCreacion() {
        return fkSeguridadCreacion;
    }

    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) {
        this.fkSeguridadCreacion = fkSeguridadCreacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getFkSeguridadEdicion() {
        return fkSeguridadEdicion;
    }

    public void setFkSeguridadEdicion(Integer fkSeguridadEdicion) {
        this.fkSeguridadEdicion = fkSeguridadEdicion;
    }

    public LocalDateTime getFechaEdicion() {
        return fechaEdicion;
    }

    public void setFechaEdicion(LocalDateTime fechaEdicion) {
        this.fechaEdicion = fechaEdicion;
    }
}

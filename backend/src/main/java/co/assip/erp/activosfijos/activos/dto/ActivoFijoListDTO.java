package co.assip.erp.activosfijos.activos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ActivoFijoListDTO {

    private Long idActivoFijo;
    private String placaActivo;
    private String nombreActivo;
    private LocalDate fechaIngreso;
    private Integer mesesDepreciacion;
    private BigDecimal valorAdquisicion;
    private BigDecimal valorMensual;
    private Integer idAgencia;
    private String nombreAgencia;
    private Integer idEstadoActivo;
    private String nombreEstadoActivo;

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

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getMesesDepreciacion() {
        return mesesDepreciacion;
    }

    public void setMesesDepreciacion(Integer mesesDepreciacion) {
        this.mesesDepreciacion = mesesDepreciacion;
    }

    public BigDecimal getValorAdquisicion() {
        return valorAdquisicion;
    }

    public void setValorAdquisicion(BigDecimal valorAdquisicion) {
        this.valorAdquisicion = valorAdquisicion;
    }

    public BigDecimal getValorMensual() {
        return valorMensual;
    }

    public void setValorMensual(BigDecimal valorMensual) {
        this.valorMensual = valorMensual;
    }

    public Integer getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Integer idAgencia) {
        this.idAgencia = idAgencia;
    }

    public String getNombreAgencia() {
        return nombreAgencia;
    }

    public void setNombreAgencia(String nombreAgencia) {
        this.nombreAgencia = nombreAgencia;
    }

    public Integer getIdEstadoActivo() {
        return idEstadoActivo;
    }

    public void setIdEstadoActivo(Integer idEstadoActivo) {
        this.idEstadoActivo = idEstadoActivo;
    }

    public String getNombreEstadoActivo() {
        return nombreEstadoActivo;
    }

    public void setNombreEstadoActivo(String nombreEstadoActivo) {
        this.nombreEstadoActivo = nombreEstadoActivo;
    }
}

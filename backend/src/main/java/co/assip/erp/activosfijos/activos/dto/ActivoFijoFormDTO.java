package co.assip.erp.activosfijos.activos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ActivoFijoFormDTO {

    private Long idActivoFijo;

    private String placaActivo;
    private String nombreActivo;

    private LocalDate fechaIngreso;
    private LocalDate fechaGarantia;
    private LocalDate fechaBaja;

    private Integer idFormaDepreciacion;
    private Integer mesesDepreciacion;

    private BigDecimal valorAdquisicion;
    private BigDecimal valorMensual;

    private Integer idEstadoActivo;
    private Integer idTipoAdquisicion;
    private Integer idAgencia;
    private Long idUbicacion;
    private Integer idBloque;

    private Long idDatosPersonalResponsable;
    private Long idDatosPersonalProveedor;

    private Long idCatalogoCuentaActivo;
    private Long idCatalogoCuentaDepreciacion;
    private Long idCatalogoCuentaGasto;
    private Long idCatalogoCuentaControl;

    // =========================
    // Getters & Setters
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

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaGarantia() {
        return fechaGarantia;
    }

    public void setFechaGarantia(LocalDate fechaGarantia) {
        this.fechaGarantia = fechaGarantia;
    }

    public LocalDate getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDate fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public Integer getIdFormaDepreciacion() {
        return idFormaDepreciacion;
    }

    public void setIdFormaDepreciacion(Integer idFormaDepreciacion) {
        this.idFormaDepreciacion = idFormaDepreciacion;
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

    public Integer getIdEstadoActivo() {
        return idEstadoActivo;
    }

    public void setIdEstadoActivo(Integer idEstadoActivo) {
        this.idEstadoActivo = idEstadoActivo;
    }

    public Integer getIdTipoAdquisicion() {
        return idTipoAdquisicion;
    }

    public void setIdTipoAdquisicion(Integer idTipoAdquisicion) {
        this.idTipoAdquisicion = idTipoAdquisicion;
    }

    public Integer getIdAgencia() {
        return idAgencia;
    }

    public void setIdAgencia(Integer idAgencia) {
        this.idAgencia = idAgencia;
    }

    public Long getIdUbicacion() {
        return idUbicacion;
    }

    public void setIdUbicacion(Long idUbicacion) {
        this.idUbicacion = idUbicacion;
    }

    public Integer getIdBloque() {
        return idBloque;
    }

    public void setIdBloque(Integer idBloque) {
        this.idBloque = idBloque;
    }

    public Long getIdDatosPersonalResponsable() {
        return idDatosPersonalResponsable;
    }

    public void setIdDatosPersonalResponsable(Long idDatosPersonalResponsable) {
        this.idDatosPersonalResponsable = idDatosPersonalResponsable;
    }

    public Long getIdDatosPersonalProveedor() {
        return idDatosPersonalProveedor;
    }

    public void setIdDatosPersonalProveedor(Long idDatosPersonalProveedor) {
        this.idDatosPersonalProveedor = idDatosPersonalProveedor;
    }

    public Long getIdCatalogoCuentaActivo() {
        return idCatalogoCuentaActivo;
    }

    public void setIdCatalogoCuentaActivo(Long idCatalogoCuentaActivo) {
        this.idCatalogoCuentaActivo = idCatalogoCuentaActivo;
    }

    public Long getIdCatalogoCuentaDepreciacion() {
        return idCatalogoCuentaDepreciacion;
    }

    public void setIdCatalogoCuentaDepreciacion(Long idCatalogoCuentaDepreciacion) {
        this.idCatalogoCuentaDepreciacion = idCatalogoCuentaDepreciacion;
    }

    public Long getIdCatalogoCuentaGasto() {
        return idCatalogoCuentaGasto;
    }

    public void setIdCatalogoCuentaGasto(Long idCatalogoCuentaGasto) {
        this.idCatalogoCuentaGasto = idCatalogoCuentaGasto;
    }

    public Long getIdCatalogoCuentaControl() {
        return idCatalogoCuentaControl;
    }

    public void setIdCatalogoCuentaControl(Long idCatalogoCuentaControl) {
        this.idCatalogoCuentaControl = idCatalogoCuentaControl;
    }

}

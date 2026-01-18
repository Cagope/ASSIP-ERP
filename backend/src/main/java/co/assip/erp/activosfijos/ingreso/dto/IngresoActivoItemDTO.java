package co.assip.erp.activosfijos.ingreso.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IngresoActivoItemDTO {

    @NotBlank
    private String placaActivo;

    @NotBlank
    private String nombreActivo;

    @NotNull
    private Long idUbicacion;

    @NotNull
    private Long idBloque;

    @NotNull
    private Long idFormaDepreciacion;

    @NotNull
    private Long idTipoAdquisicion;

    @NotNull
    private Long idEstadoActivo;

    private Long idResponsable;
    private Long idProveedor; // opcional: si no llega, se usa el del header

    @NotNull
    private Long idCuentaActivo;

    private Long idCuentaIvaActivo;

    @NotNull
    private Long idCuentaDepreciacion;

    @NotNull
    private Long idCuentaGasto;

    @NotNull
    private Long idCuentaControl;

    private Long idCuentaRetencion;

    private LocalDate fechaGarantia;

    @NotNull
    @Min(1)
    private Integer mesesDepreciacion;

    @NotNull
    private BigDecimal valorHistorico;

    @NotNull
    private BigDecimal valorIva;

    @NotNull
    private BigDecimal valorRetencion;

    public String getPlacaActivo() { return placaActivo; }
    public void setPlacaActivo(String placaActivo) { this.placaActivo = placaActivo; }

    public String getNombreActivo() { return nombreActivo; }
    public void setNombreActivo(String nombreActivo) { this.nombreActivo = nombreActivo; }

    public Long getIdUbicacion() { return idUbicacion; }
    public void setIdUbicacion(Long idUbicacion) { this.idUbicacion = idUbicacion; }

    public Long getIdBloque() { return idBloque; }
    public void setIdBloque(Long idBloque) { this.idBloque = idBloque; }

    public Long getIdFormaDepreciacion() { return idFormaDepreciacion; }
    public void setIdFormaDepreciacion(Long idFormaDepreciacion) { this.idFormaDepreciacion = idFormaDepreciacion; }

    public Long getIdTipoAdquisicion() { return idTipoAdquisicion; }
    public void setIdTipoAdquisicion(Long idTipoAdquisicion) { this.idTipoAdquisicion = idTipoAdquisicion; }

    public Long getIdEstadoActivo() { return idEstadoActivo; }
    public void setIdEstadoActivo(Long idEstadoActivo) { this.idEstadoActivo = idEstadoActivo; }

    public Long getIdResponsable() { return idResponsable; }
    public void setIdResponsable(Long idResponsable) { this.idResponsable = idResponsable; }

    public Long getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }

    public Long getIdCuentaActivo() { return idCuentaActivo; }
    public void setIdCuentaActivo(Long idCuentaActivo) { this.idCuentaActivo = idCuentaActivo; }

    public Long getIdCuentaIvaActivo() { return idCuentaIvaActivo; }
    public void setIdCuentaIvaActivo(Long idCuentaIvaActivo) { this.idCuentaIvaActivo = idCuentaIvaActivo; }

    public Long getIdCuentaDepreciacion() { return idCuentaDepreciacion; }
    public void setIdCuentaDepreciacion(Long idCuentaDepreciacion) { this.idCuentaDepreciacion = idCuentaDepreciacion; }

    public Long getIdCuentaGasto() { return idCuentaGasto; }
    public void setIdCuentaGasto(Long idCuentaGasto) { this.idCuentaGasto = idCuentaGasto; }

    public Long getIdCuentaControl() { return idCuentaControl; }
    public void setIdCuentaControl(Long idCuentaControl) { this.idCuentaControl = idCuentaControl; }

    public Long getIdCuentaRetencion() { return idCuentaRetencion; }
    public void setIdCuentaRetencion(Long idCuentaRetencion) { this.idCuentaRetencion = idCuentaRetencion; }

    public LocalDate getFechaGarantia() { return fechaGarantia; }
    public void setFechaGarantia(LocalDate fechaGarantia) { this.fechaGarantia = fechaGarantia; }

    public Integer getMesesDepreciacion() { return mesesDepreciacion; }
    public void setMesesDepreciacion(Integer mesesDepreciacion) { this.mesesDepreciacion = mesesDepreciacion; }

    public BigDecimal getValorHistorico() { return valorHistorico; }
    public void setValorHistorico(BigDecimal valorHistorico) { this.valorHistorico = valorHistorico; }

    public BigDecimal getValorIva() { return valorIva; }
    public void setValorIva(BigDecimal valorIva) { this.valorIva = valorIva; }

    public BigDecimal getValorRetencion() { return valorRetencion; }
    public void setValorRetencion(BigDecimal valorRetencion) { this.valorRetencion = valorRetencion; }
}

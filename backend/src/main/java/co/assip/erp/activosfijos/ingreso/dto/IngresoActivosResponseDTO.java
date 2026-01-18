package co.assip.erp.activosfijos.ingreso.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class IngresoActivosResponseDTO {

    private Long idComprobante;
    private String tipoComprobante;
    private String numeroComprobante;
    private LocalDate fechaInclusion;

    private Integer cantidadActivos;
    private BigDecimal totalDebito;
    private BigDecimal totalCredito;

    private List<Long> idsActivosCreados;

    public Long getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Long idComprobante) { this.idComprobante = idComprobante; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getNumeroComprobante() { return numeroComprobante; }
    public void setNumeroComprobante(String numeroComprobante) { this.numeroComprobante = numeroComprobante; }

    public LocalDate getFechaInclusion() { return fechaInclusion; }
    public void setFechaInclusion(LocalDate fechaInclusion) { this.fechaInclusion = fechaInclusion; }

    public Integer getCantidadActivos() { return cantidadActivos; }
    public void setCantidadActivos(Integer cantidadActivos) { this.cantidadActivos = cantidadActivos; }

    public BigDecimal getTotalDebito() { return totalDebito; }
    public void setTotalDebito(BigDecimal totalDebito) { this.totalDebito = totalDebito; }

    public BigDecimal getTotalCredito() { return totalCredito; }
    public void setTotalCredito(BigDecimal totalCredito) { this.totalCredito = totalCredito; }

    public List<Long> getIdsActivosCreados() { return idsActivosCreados; }
    public void setIdsActivosCreados(List<Long> idsActivosCreados) { this.idsActivosCreados = idsActivosCreados; }
}

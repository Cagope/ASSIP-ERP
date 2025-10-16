package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(schema = "catalogos", name = "tipos_regimen")
public class TipoRegimenCat {

    @Id
    @Column(name = "codigo_regimen", length = 2)
    private String codigoRegimen;

    @Column(name = "nombre_regimen", length = 50, nullable = false)
    private String nombreRegimen;

    @Column(name = "porcentaje_retencion")
    private BigDecimal porcentajeRetencion;

    @Column(name = "base_retencion", nullable = false)
    private Boolean baseRetencion;

    public String getCodigoRegimen() { return codigoRegimen; }
    public void setCodigoRegimen(String codigoRegimen) { this.codigoRegimen = codigoRegimen; }
    public String getNombreRegimen() { return nombreRegimen; }
    public void setNombreRegimen(String nombreRegimen) { this.nombreRegimen = nombreRegimen; }
    public BigDecimal getPorcentajeRetencion() { return porcentajeRetencion; }
    public void setPorcentajeRetencion(BigDecimal porcentajeRetencion) { this.porcentajeRetencion = porcentajeRetencion; }
    public Boolean getBaseRetencion() { return baseRetencion; }
    public void setBaseRetencion(Boolean baseRetencion) { this.baseRetencion = baseRetencion; }
}

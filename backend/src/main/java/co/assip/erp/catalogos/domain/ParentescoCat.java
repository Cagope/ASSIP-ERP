package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "parentescos", schema = "catalogos")
public class ParentescoCat {

    @Id
    @Column(name = "codigo_parentesco", length = 2)
    private String codigoParentesco;

    @Column(name = "nombre_parentesco", length = 50, nullable = false)
    private String nombreParentesco;

    public String getCodigoParentesco() { return codigoParentesco; }
    public void setCodigoParentesco(String codigoParentesco) { this.codigoParentesco = codigoParentesco; }

    public String getNombreParentesco() { return nombreParentesco; }
    public void setNombreParentesco(String nombreParentesco) { this.nombreParentesco = nombreParentesco; }
}

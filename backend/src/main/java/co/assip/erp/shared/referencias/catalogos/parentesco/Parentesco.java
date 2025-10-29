package co.assip.erp.shared.referencias.catalogos.parentesco;

import jakarta.persistence.*;

@Entity
@Table(name = "parentescos", schema = "catalogos")
public class Parentesco {

    @Id
    @Column(name = "codigo_parentesco", length = 2)
    private String codigoParentesco;

    @Column(name = "nombre_parentesco", length = 50, nullable = false)
    private String nombreParentesco;

    // Getters y Setters
    public String getCodigoParentesco() {
        return codigoParentesco;
    }

    public void setCodigoParentesco(String codigoParentesco) {
        this.codigoParentesco = codigoParentesco;
    }

    public String getNombreParentesco() {
        return nombreParentesco;
    }

    public void setNombreParentesco(String nombreParentesco) {
        this.nombreParentesco = nombreParentesco;
    }
}

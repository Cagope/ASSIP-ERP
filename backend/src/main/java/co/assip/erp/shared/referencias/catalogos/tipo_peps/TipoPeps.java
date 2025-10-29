package co.assip.erp.shared.referencias.catalogos.tipo_peps;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_peps", schema = "catalogos")
public class TipoPeps {

    @Id
    @Column(name = "tipo_peps", length = 5)
    private String tipoPeps;

    @Column(name = "nombre_tipo_peps", length = 250, nullable = false)
    private String nombreTipoPeps;

    // Getters y Setters
    public String getTipoPeps() {
        return tipoPeps;
    }

    public void setTipoPeps(String tipoPeps) {
        this.tipoPeps = tipoPeps;
    }

    public String getNombreTipoPeps() {
        return nombreTipoPeps;
    }

    public void setNombreTipoPeps(String nombreTipoPeps) {
        this.nombreTipoPeps = nombreTipoPeps;
    }
}

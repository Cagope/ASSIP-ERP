package co.assip.erp.shared.referencias.catalogos.tipo_bien;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_bienes", schema = "catalogos")
public class TipoBien {

    @Id
    @Column(name = "codigo_tipo_bien", length = 2)
    private String codigoTipoBien;

    @Column(name = "descripcion_tipo_bien", length = 100)
    private String descripcionTipoBien;

    // Getters y Setters
    public String getCodigoTipoBien() {
        return codigoTipoBien;
    }

    public void setCodigoTipoBien(String codigoTipoBien) {
        this.codigoTipoBien = codigoTipoBien;
    }

    public String getDescripcionTipoBien() {
        return descripcionTipoBien;
    }

    public void setDescripcionTipoBien(String descripcionTipoBien) {
        this.descripcionTipoBien = descripcionTipoBien;
    }
}

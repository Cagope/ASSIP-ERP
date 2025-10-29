package co.assip.erp.shared.referencias.catalogos.genero;

import jakarta.persistence.*;

@Entity
@Table(name = "generos", schema = "catalogos")
public class Genero {

    @Id
    @Column(name = "codigo_genero", length = 1)
    private String codigoGenero;

    @Column(name = "nombre_genero", length = 50)
    private String nombreGenero;

    // Getters y Setters
    public String getCodigoGenero() {
        return codigoGenero;
    }

    public void setCodigoGenero(String codigoGenero) {
        this.codigoGenero = codigoGenero;
    }

    public String getNombreGenero() {
        return nombreGenero;
    }

    public void setNombreGenero(String nombreGenero) {
        this.nombreGenero = nombreGenero;
    }
}

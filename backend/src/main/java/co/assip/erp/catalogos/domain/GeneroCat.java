package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "generos")
public class GeneroCat {

    @Id
    @Column(name = "codigo_genero", length = 1)
    private String codigoGenero;

    @Column(name = "nombre_genero", length = 50)
    private String nombreGenero;

    public String getCodigoGenero() { return codigoGenero; }
    public void setCodigoGenero(String codigoGenero) { this.codigoGenero = codigoGenero; }
    public String getNombreGenero() { return nombreGenero; }
    public void setNombreGenero(String nombreGenero) { this.nombreGenero = nombreGenero; }
}

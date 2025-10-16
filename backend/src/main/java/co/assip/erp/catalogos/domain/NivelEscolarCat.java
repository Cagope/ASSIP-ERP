package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "niveles_escolares")
public class NivelEscolarCat {

    @Id
    @Column(name = "codigo_escolaridad", length = 2)
    private String codigoEscolaridad;

    @Column(name = "nombre_escolaridad", length = 100)
    private String nombreEscolaridad;

    public String getCodigoEscolaridad() { return codigoEscolaridad; }
    public void setCodigoEscolaridad(String codigoEscolaridad) { this.codigoEscolaridad = codigoEscolaridad; }
    public String getNombreEscolaridad() { return nombreEscolaridad; }
    public void setNombreEscolaridad(String nombreEscolaridad) { this.nombreEscolaridad = nombreEscolaridad; }
}

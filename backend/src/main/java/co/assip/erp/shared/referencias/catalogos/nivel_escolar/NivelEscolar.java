package co.assip.erp.shared.referencias.catalogos.nivel_escolar;

import jakarta.persistence.*;

@Entity
@Table(name = "niveles_escolares", schema = "catalogos")
public class NivelEscolar {

    @Id
    @Column(name = "codigo_escolaridad", length = 2)
    private String codigoEscolaridad;

    @Column(name = "nombre_escolaridad", length = 100)
    private String nombreEscolaridad;

    // Getters y Setters
    public String getCodigoEscolaridad() {
        return codigoEscolaridad;
    }

    public void setCodigoEscolaridad(String codigoEscolaridad) {
        this.codigoEscolaridad = codigoEscolaridad;
    }

    public String getNombreEscolaridad() {
        return nombreEscolaridad;
    }

    public void setNombreEscolaridad(String nombreEscolaridad) {
        this.nombreEscolaridad = nombreEscolaridad;
    }
}

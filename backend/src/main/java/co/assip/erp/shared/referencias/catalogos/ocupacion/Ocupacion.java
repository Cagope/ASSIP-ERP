package co.assip.erp.shared.referencias.catalogos.ocupacion;

import jakarta.persistence.*;

@Entity
@Table(name = "ocupaciones", schema = "catalogos")
public class Ocupacion {

    @Id
    @Column(name = "codigo_ocupacion", length = 2)
    private String codigoOcupacion;

    @Column(name = "nombre_ocupacion", length = 50)
    private String nombreOcupacion;

    // Getters y Setters
    public String getCodigoOcupacion() {
        return codigoOcupacion;
    }

    public void setCodigoOcupacion(String codigoOcupacion) {
        this.codigoOcupacion = codigoOcupacion;
    }

    public String getNombreOcupacion() {
        return nombreOcupacion;
    }

    public void setNombreOcupacion(String nombreOcupacion) {
        this.nombreOcupacion = nombreOcupacion;
    }
}

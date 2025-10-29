package co.assip.erp.shared.referencias.catalogos.estado_civil;

import jakarta.persistence.*;

@Entity
@Table(name = "estados_civiles", schema = "catalogos")
public class EstadoCivil {

    @Id
    @Column(name = "codigo_estado_civil", length = 1)
    private String codigoEstadoCivil;

    @Column(name = "nombre_estado_civil", length = 20)
    private String nombreEstadoCivil;

    // Getters y Setters
    public String getCodigoEstadoCivil() {
        return codigoEstadoCivil;
    }

    public void setCodigoEstadoCivil(String codigoEstadoCivil) {
        this.codigoEstadoCivil = codigoEstadoCivil;
    }

    public String getNombreEstadoCivil() {
        return nombreEstadoCivil;
    }

    public void setNombreEstadoCivil(String nombreEstadoCivil) {
        this.nombreEstadoCivil = nombreEstadoCivil;
    }
}

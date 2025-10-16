package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "estados_civiles")
public class EstadoCivilCat {

    @Id
    @Column(name = "codigo_estado_civil", length = 1)
    private String codigoEstadoCivil;

    @Column(name = "nombre_estado_civil", length = 20)
    private String nombreEstadoCivil;

    public String getCodigoEstadoCivil() { return codigoEstadoCivil; }
    public void setCodigoEstadoCivil(String codigoEstadoCivil) { this.codigoEstadoCivil = codigoEstadoCivil; }
    public String getNombreEstadoCivil() { return nombreEstadoCivil; }
    public void setNombreEstadoCivil(String nombreEstadoCivil) { this.nombreEstadoCivil = nombreEstadoCivil; }
}

package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "ocupaciones")
public class OcupacionCat {

    @Id
    @Column(name = "codigo_ocupacion", length = 2)
    private String codigoOcupacion;

    @Column(name = "nombre_ocupacion", length = 50)
    private String nombreOcupacion;

    public String getCodigoOcupacion() { return codigoOcupacion; }
    public void setCodigoOcupacion(String codigoOcupacion) { this.codigoOcupacion = codigoOcupacion; }
    public String getNombreOcupacion() { return nombreOcupacion; }
    public void setNombreOcupacion(String nombreOcupacion) { this.nombreOcupacion = nombreOcupacion; }
}

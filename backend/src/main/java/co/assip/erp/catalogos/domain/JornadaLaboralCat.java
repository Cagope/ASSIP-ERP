package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(schema = "catalogos", name = "jornadas_laborales")
public class JornadaLaboralCat {

    @Id
    @Column(name = "codigo_jornada", length = 2)
    private String codigoJornada;

    @Column(name = "nombre_jornada", length = 50)
    private String nombreJornada;

    // Getters y Setters
    public String getCodigoJornada() {
        return codigoJornada;
    }

    public void setCodigoJornada(String codigoJornada) {
        this.codigoJornada = codigoJornada;
    }

    public String getNombreJornada() {
        return nombreJornada;
    }

    public void setNombreJornada(String nombreJornada) {
        this.nombreJornada = nombreJornada;
    }
}

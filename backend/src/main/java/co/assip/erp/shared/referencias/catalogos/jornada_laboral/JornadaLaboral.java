package co.assip.erp.shared.referencias.catalogos.jornada_laboral;

import jakarta.persistence.*;

@Entity
@Table(name = "jornadas_laborales", schema = "catalogos")
public class JornadaLaboral {

    @Id
    @Column(name = "codigo_jornada", length = 2)
    private String codigoJornada;

    @Column(name = "nombre_jornada", length = 50, nullable = false)
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

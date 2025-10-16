package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "actividades_economicas_ses", schema = "catalogos")
public class ActividadEconomicaSes {

    @Id
    @Column(name = "codigo_actividad_ses", length = 4, nullable = false)
    private String codigoActividadSes;

    @Column(name = "nombre_actividad_ses", length = 200)
    private String nombreActividadSes;

    public ActividadEconomicaSes() {}

    public String getCodigoActividadSes() {
        return codigoActividadSes;
    }

    public void setCodigoActividadSes(String codigoActividadSes) {
        this.codigoActividadSes = codigoActividadSes;
    }

    public String getNombreActividadSes() {
        return nombreActividadSes;
    }

    public void setNombreActividadSes(String nombreActividadSes) {
        this.nombreActividadSes = nombreActividadSes;
    }
}

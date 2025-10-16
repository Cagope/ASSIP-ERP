package co.assip.erp.catalogos.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "actividades_economicas_dian", schema = "catalogos")
public class ActividadEconomicaDian {

    @Id
    @Column(name = "codigo_actividad_dian", length = 4, nullable = false)
    private String codigoActividadDian;

    @Column(name = "nombre_actividad_dian", length = 200)
    private String nombreActividadDian;

    public ActividadEconomicaDian() {}

    public String getCodigoActividadDian() {
        return codigoActividadDian;
    }

    public void setCodigoActividadDian(String codigoActividadDian) {
        this.codigoActividadDian = codigoActividadDian;
    }

    public String getNombreActividadDian() {
        return nombreActividadDian;
    }

    public void setNombreActividadDian(String nombreActividadDian) {
        this.nombreActividadDian = nombreActividadDian;
    }
}

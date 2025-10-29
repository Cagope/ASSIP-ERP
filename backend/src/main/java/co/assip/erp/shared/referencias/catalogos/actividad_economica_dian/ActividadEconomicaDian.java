package co.assip.erp.shared.referencias.catalogos.actividad_economica_dian;

import jakarta.persistence.*;

@Entity
@Table(name = "actividades_economicas_dian", schema = "catalogos")
public class ActividadEconomicaDian {

    @Id
    @Column(name = "codigo_actividad_dian", length = 4)
    private String codigoActividadDian;

    @Column(name = "nombre_actividad_dian", length = 200, nullable = false)
    private String nombreActividadDian;

    // Getters y Setters
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

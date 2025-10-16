package co.assip.erp.hojavida.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "permisos_especiales", schema = "hoja_vida",
        uniqueConstraints = @UniqueConstraint(name = "uk_permiso_persona", columnNames = "id_datos_personal"))
public class PermisoEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso_especial")
    private Integer idPermisoEspecial;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(name = "recibe_llamadas", nullable = false)
    private Boolean recibeLlamadas = false;

    @Column(name = "recibe_msm", nullable = false)
    private Boolean recibeMsm = false;

    @Column(name = "recibe_emails", nullable = false)
    private Boolean recibeEmails = false;

    @Column(name = "recibe_cartas", nullable = false)
    private Boolean recibeCartas = false;

    @Column(name = "recibe_redes_sociales", nullable = false)
    private Boolean recibeRedesSociales = false;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Getters/Setters
    public Integer getIdPermisoEspecial() { return idPermisoEspecial; }
    public void setIdPermisoEspecial(Integer idPermisoEspecial) { this.idPermisoEspecial = idPermisoEspecial; }

    public Integer getIdDatosPersonal() { return idDatosPersonal; }
    public void setIdDatosPersonal(Integer idDatosPersonal) { this.idDatosPersonal = idDatosPersonal; }

    public Boolean getRecibeLlamadas() { return recibeLlamadas; }
    public void setRecibeLlamadas(Boolean recibeLlamadas) { this.recibeLlamadas = recibeLlamadas; }

    public Boolean getRecibeMsm() { return recibeMsm; }
    public void setRecibeMsm(Boolean recibeMsm) { this.recibeMsm = recibeMsm; }

    public Boolean getRecibeEmails() { return recibeEmails; }
    public void setRecibeEmails(Boolean recibeEmails) { this.recibeEmails = recibeEmails; }

    public Boolean getRecibeCartas() { return recibeCartas; }
    public void setRecibeCartas(Boolean recibeCartas) { this.recibeCartas = recibeCartas; }

    public Boolean getRecibeRedesSociales() { return recibeRedesSociales; }
    public void setRecibeRedesSociales(Boolean recibeRedesSociales) { this.recibeRedesSociales = recibeRedesSociales; }

    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) { this.fkSeguridadCreacion = fkSeguridadCreacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer fkSeguridadActualizacion) { this.fkSeguridadActualizacion = fkSeguridadActualizacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}

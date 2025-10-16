package co.assip.erp.hojavida.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "referencias_personales", schema = "hoja_vida")
public class ReferenciaPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_referencia_personal")
    private Integer idReferenciaPersonal;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(name = "nombre_referencia_personal", nullable = false, length = 100)
    private String nombreReferenciaPersonal;

    @Column(name = "direccion_referencia_personal", nullable = false, length = 100)
    private String direccionReferenciaPersonal;

    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @Column(name = "telefono_referencia_personal", length = 7)
    private String telefonoReferenciaPersonal; // opcional (7 dígitos si viene)

    @Column(name = "celular_referencia_personal", length = 10)
    private String celularReferenciaPersonal;  // opcional (10 dígitos si viene)

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Getters y Setters
    public Integer getIdReferenciaPersonal() { return idReferenciaPersonal; }
    public void setIdReferenciaPersonal(Integer idReferenciaPersonal) { this.idReferenciaPersonal = idReferenciaPersonal; }

    public Integer getIdDatosPersonal() { return idDatosPersonal; }
    public void setIdDatosPersonal(Integer idDatosPersonal) { this.idDatosPersonal = idDatosPersonal; }

    public String getNombreReferenciaPersonal() { return nombreReferenciaPersonal; }
    public void setNombreReferenciaPersonal(String nombreReferenciaPersonal) { this.nombreReferenciaPersonal = nombreReferenciaPersonal; }

    public String getDireccionReferenciaPersonal() { return direccionReferenciaPersonal; }
    public void setDireccionReferenciaPersonal(String direccionReferenciaPersonal) { this.direccionReferenciaPersonal = direccionReferenciaPersonal; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }

    public Integer getIdCiudad() { return idCiudad; }
    public void setIdCiudad(Integer idCiudad) { this.idCiudad = idCiudad; }

    public String getTelefonoReferenciaPersonal() { return telefonoReferenciaPersonal; }
    public void setTelefonoReferenciaPersonal(String telefonoReferenciaPersonal) { this.telefonoReferenciaPersonal = telefonoReferenciaPersonal; }

    public String getCelularReferenciaPersonal() { return celularReferenciaPersonal; }
    public void setCelularReferenciaPersonal(String celularReferenciaPersonal) { this.celularReferenciaPersonal = celularReferenciaPersonal; }

    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) { this.fkSeguridadCreacion = fkSeguridadCreacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer fkSeguridadActualizacion) { this.fkSeguridadActualizacion = fkSeguridadActualizacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}

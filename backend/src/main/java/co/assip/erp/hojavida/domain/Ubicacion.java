package co.assip.erp.hojavida.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ubicaciones", schema = "hoja_vida",
        uniqueConstraints = @UniqueConstraint(name = "uk_ubicacion_persona", columnNames = "id_datos_personal"))
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Integer idUbicacion;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(name = "direccion", nullable = false, length = 100)
    private String direccion;

    @Column(name = "barrio", nullable = false, length = 100)
    private String barrio;

    @Column(name = "telefono", length = 7)
    private String telefono; // opcional (validar 7 dígitos si viene)

    @Column(name = "celular_uno", length = 10)
    private String celularUno; // opcional (10 dígitos si viene)

    @Column(name = "celular_dos", length = 10)
    private String celularDos; // opcional (10 dígitos si viene)

    @Column(name = "correo", length = 100)
    private String correo; // opcional (regex simple si viene)

    @Column(name = "id_pais")
    private Integer idPais; // opcional (NO se valida dept ∈ país)

    @Column(name = "id_departamento")
    private Integer idDepartamento; // opcional

    @Column(name = "id_ciudad")
    private Integer idCiudad; // opcional (si viene, validar ciudad ∈ departamento)

    @Column(name = "id_sub_zona")
    private Integer idSubZona; // opcional (si viene, validar existe; zona implícita por sub-zona)

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Getters/Setters
    public Integer getIdUbicacion() { return idUbicacion; }
    public void setIdUbicacion(Integer idUbicacion) { this.idUbicacion = idUbicacion; }

    public Integer getIdDatosPersonal() { return idDatosPersonal; }
    public void setIdDatosPersonal(Integer idDatosPersonal) { this.idDatosPersonal = idDatosPersonal; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = barrio; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCelularUno() { return celularUno; }
    public void setCelularUno(String celularUno) { this.celularUno = celularUno; }

    public String getCelularDos() { return celularDos; }
    public void setCelularDos(String celularDos) { this.celularDos = celularDos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Integer getIdPais() { return idPais; }
    public void setIdPais(Integer idPais) { this.idPais = idPais; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }

    public Integer getIdCiudad() { return idCiudad; }
    public void setIdCiudad(Integer idCiudad) { this.idCiudad = idCiudad; }

    public Integer getIdSubZona() { return idSubZona; }
    public void setIdSubZona(Integer idSubZona) { this.idSubZona = idSubZona; }

    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) { this.fkSeguridadCreacion = fkSeguridadCreacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer fkSeguridadActualizacion) { this.fkSeguridadActualizacion = fkSeguridadActualizacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}

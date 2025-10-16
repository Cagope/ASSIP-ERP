package co.assip.erp.hojavida.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "laborales", schema = "hoja_vida")
public class Laboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_laboral")
    private Integer idLaboral;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(name = "nombre_empresa", length = 100, nullable = false)
    private String nombreEmpresa;

    @Column(name = "direccion", length = 100, nullable = false)
    private String direccion;

    @Column(name = "id_pais")
    private Integer idPais;

    @Column(name = "id_departamento")
    private Integer idDepartamento;

    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @Column(name = "telefono_empresa", length = 7)
    private String telefonoEmpresa;

    @Column(name = "celular_empresa", length = 10)
    private String celularEmpresa;

    @Column(name = "correo_empresa", length = 100)
    private String correoEmpresa;

    @Column(name = "codigo_tipo_empresa", length = 2)
    private String codigoTipoEmpresa;

    @Column(name = "empleado_entidad", nullable = false)
    private Boolean empleadoEntidad = Boolean.FALSE;

    @Column(name = "codigo_tipo_contrato", length = 2)
    private String codigoTipoContrato;

    @Column(name = "codigo_jornada", length = 2)
    private String codigoJornada;

    @Column(name = "nombre_contacto", length = 100, nullable = false)
    private String nombreContacto;

    @Column(name = "celular_contacto", length = 10)
    private String celularContacto;

    @Column(name = "fecha_vinculacion")
    private LocalDate fechaVinculacion;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // Getters y Setters
    public Integer getIdLaboral() { return idLaboral; }
    public void setIdLaboral(Integer idLaboral) { this.idLaboral = idLaboral; }

    public Integer getIdDatosPersonal() { return idDatosPersonal; }
    public void setIdDatosPersonal(Integer idDatosPersonal) { this.idDatosPersonal = idDatosPersonal; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Integer getIdPais() { return idPais; }
    public void setIdPais(Integer idPais) { this.idPais = idPais; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }

    public Integer getIdCiudad() { return idCiudad; }
    public void setIdCiudad(Integer idCiudad) { this.idCiudad = idCiudad; }

    public String getTelefonoEmpresa() { return telefonoEmpresa; }
    public void setTelefonoEmpresa(String telefonoEmpresa) { this.telefonoEmpresa = telefonoEmpresa; }

    public String getCelularEmpresa() { return celularEmpresa; }
    public void setCelularEmpresa(String celularEmpresa) { this.celularEmpresa = celularEmpresa; }

    public String getCorreoEmpresa() { return correoEmpresa; }
    public void setCorreoEmpresa(String correoEmpresa) { this.correoEmpresa = correoEmpresa; }

    public String getCodigoTipoEmpresa() { return codigoTipoEmpresa; }
    public void setCodigoTipoEmpresa(String codigoTipoEmpresa) { this.codigoTipoEmpresa = codigoTipoEmpresa; }

    public Boolean getEmpleadoEntidad() { return empleadoEntidad; }
    public void setEmpleadoEntidad(Boolean empleadoEntidad) { this.empleadoEntidad = empleadoEntidad; }

    public String getCodigoTipoContrato() { return codigoTipoContrato; }
    public void setCodigoTipoContrato(String codigoTipoContrato) { this.codigoTipoContrato = codigoTipoContrato; }

    public String getCodigoJornada() { return codigoJornada; }
    public void setCodigoJornada(String codigoJornada) { this.codigoJornada = codigoJornada; }

    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }

    public String getCelularContacto() { return celularContacto; }
    public void setCelularContacto(String celularContacto) { this.celularContacto = celularContacto; }

    public LocalDate getFechaVinculacion() { return fechaVinculacion; }
    public void setFechaVinculacion(LocalDate fechaVinculacion) { this.fechaVinculacion = fechaVinculacion; }

    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) { this.fkSeguridadCreacion = fkSeguridadCreacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer fkSeguridadActualizacion) { this.fkSeguridadActualizacion = fkSeguridadActualizacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}

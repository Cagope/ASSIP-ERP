package co.assip.erp.hojavida.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "datos_familiares", schema = "hoja_vida")
public class DatosFamiliares {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_datos_familiares")
    private Integer idDatosFamiliares;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    @Column(name = "codigo_parentesco", length = 2, nullable = false)
    private String codigoParentesco;

    @Column(name = "nombre_datos_familiar", length = 100, nullable = false)
    private String nombreDatosFamiliar;

    @Column(name = "documento_datos_familiar", length = 20, nullable = false)
    private String documentoDatosFamiliar;

    @Column(name = "telefono_datos_familiar", length = 7)
    private String telefonoDatosFamiliar; // opcional (7 dígitos si viene)

    @Column(name = "celular_datos_familiar", length = 10)
    private String celularDatosFamiliar;  // opcional (10 dígitos si viene)

    @Column(name = "direccion_datos_familiar", length = 100, nullable = false)
    private String direccionDatosFamiliar;

    @Column(name = "id_departamento", nullable = false)
    private Integer idDepartamento;

    @Column(name = "id_ciudad", nullable = false)
    private Integer idCiudad;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "ingresos_datos_familiar", precision = 18, scale = 2, nullable = false)
    private BigDecimal ingresosDatosFamiliar;

    @Column(name = "egresos_datos_familiar", precision = 18, scale = 2, nullable = false)
    private BigDecimal egresosDatosFamiliar;

    @Column(name = "referencia_familiar", nullable = false)
    private Boolean referenciaFamiliar;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // Getters y Setters
    public Integer getIdDatosFamiliares() { return idDatosFamiliares; }
    public void setIdDatosFamiliares(Integer idDatosFamiliares) { this.idDatosFamiliares = idDatosFamiliares; }

    public Integer getIdDatosPersonal() { return idDatosPersonal; }
    public void setIdDatosPersonal(Integer idDatosPersonal) { this.idDatosPersonal = idDatosPersonal; }

    public String getCodigoParentesco() { return codigoParentesco; }
    public void setCodigoParentesco(String codigoParentesco) { this.codigoParentesco = codigoParentesco; }

    public String getNombreDatosFamiliar() { return nombreDatosFamiliar; }
    public void setNombreDatosFamiliar(String nombreDatosFamiliar) { this.nombreDatosFamiliar = nombreDatosFamiliar; }

    public String getDocumentoDatosFamiliar() { return documentoDatosFamiliar; }
    public void setDocumentoDatosFamiliar(String documentoDatosFamiliar) { this.documentoDatosFamiliar = documentoDatosFamiliar; }

    public String getTelefonoDatosFamiliar() { return telefonoDatosFamiliar; }
    public void setTelefonoDatosFamiliar(String telefonoDatosFamiliar) { this.telefonoDatosFamiliar = telefonoDatosFamiliar; }

    public String getCelularDatosFamiliar() { return celularDatosFamiliar; }
    public void setCelularDatosFamiliar(String celularDatosFamiliar) { this.celularDatosFamiliar = celularDatosFamiliar; }

    public String getDireccionDatosFamiliar() { return direccionDatosFamiliar; }
    public void setDireccionDatosFamiliar(String direccionDatosFamiliar) { this.direccionDatosFamiliar = direccionDatosFamiliar; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }

    public Integer getIdCiudad() { return idCiudad; }
    public void setIdCiudad(Integer idCiudad) { this.idCiudad = idCiudad; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public BigDecimal getIngresosDatosFamiliar() { return ingresosDatosFamiliar; }
    public void setIngresosDatosFamiliar(BigDecimal ingresosDatosFamiliar) { this.ingresosDatosFamiliar = ingresosDatosFamiliar; }

    public BigDecimal getEgresosDatosFamiliar() { return egresosDatosFamiliar; }
    public void setEgresosDatosFamiliar(BigDecimal egresosDatosFamiliar) { this.egresosDatosFamiliar = egresosDatosFamiliar; }

    public Boolean getReferenciaFamiliar() { return referenciaFamiliar; }
    public void setReferenciaFamiliar(Boolean referenciaFamiliar) { this.referenciaFamiliar = referenciaFamiliar; }

    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) { this.fkSeguridadCreacion = fkSeguridadCreacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer fkSeguridadActualizacion) { this.fkSeguridadActualizacion = fkSeguridadActualizacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}

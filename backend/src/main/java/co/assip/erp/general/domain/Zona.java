package co.assip.erp.general.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "zonas", schema = "general")
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona")
    private Integer idZona;

    @Column(name = "codigo_zona", length = 3)
    private String codigoZona;

    @Column(name = "nombre_zona", length = 100)
    private String nombreZona;

    @Column(name = "comentario_zona", length = 100)
    private String comentarioZona;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Getters & Setters
    public Integer getIdZona() { return idZona; }
    public void setIdZona(Integer idZona) { this.idZona = idZona; }
    public String getCodigoZona() { return codigoZona; }
    public void setCodigoZona(String codigoZona) { this.codigoZona = codigoZona; }
    public String getNombreZona() { return nombreZona; }
    public void setNombreZona(String nombreZona) { this.nombreZona = nombreZona; }
    public String getComentarioZona() { return comentarioZona; }
    public void setComentarioZona(String comentarioZona) { this.comentarioZona = comentarioZona; }
    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) { this.fkSeguridadCreacion = fkSeguridadCreacion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer fkSeguridadActualizacion) { this.fkSeguridadActualizacion = fkSeguridadActualizacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}

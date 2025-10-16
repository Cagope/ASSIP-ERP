package co.assip.erp.general.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sub_zonas", schema = "general",
        uniqueConstraints = @UniqueConstraint(name = "uk_sub_zonas", columnNames = {"id_zona", "codigo_sub_zona"}))
public class SubZona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sub_zona")
    private Integer idSubZona;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_zona", nullable = false)
    private Zona zona; // FK a general.zonas

    @Column(name = "codigo_sub_zona", length = 3, nullable = false)
    private String codigoSubZona;

    @Column(name = "nombre_sub_zona", length = 100, nullable = false)
    private String nombreSubZona;

    @Column(name = "comentario_sub_zona", length = 100, nullable = false)
    private String comentarioSubZona;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Getters & Setters
    public Integer getIdSubZona() { return idSubZona; }
    public void setIdSubZona(Integer idSubZona) { this.idSubZona = idSubZona; }

    public Zona getZona() { return zona; }
    public void setZona(Zona zona) { this.zona = zona; }

    public String getCodigoSubZona() { return codigoSubZona; }
    public void setCodigoSubZona(String codigoSubZona) { this.codigoSubZona = codigoSubZona; }

    public String getNombreSubZona() { return nombreSubZona; }
    public void setNombreSubZona(String nombreSubZona) { this.nombreSubZona = nombreSubZona; }

    public String getComentarioSubZona() { return comentarioSubZona; }
    public void setComentarioSubZona(String comentarioSubZona) { this.comentarioSubZona = comentarioSubZona; }

    public Integer getFkSeguridadCreacion() { return fkSeguridadCreacion; }
    public void setFkSeguridadCreacion(Integer fkSeguridadCreacion) { this.fkSeguridadCreacion = fkSeguridadCreacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getFkSeguridadActualizacion() { return fkSeguridadActualizacion; }
    public void setFkSeguridadActualizacion(Integer fkSeguridadActualizacion) { this.fkSeguridadActualizacion = fkSeguridadActualizacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}

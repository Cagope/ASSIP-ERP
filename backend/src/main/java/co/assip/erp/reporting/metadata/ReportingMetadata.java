package co.assip.erp.reporting.metadata;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;

/**
 * 🗂 ReportingMetadata
 * ==============================================================
 * Representa los metadatos de una vista del esquema `reporting`.
 * Cada registro describe una vista disponible para consultas seguras.
 *
 * Fuente: información obtenida dinámicamente desde pg_catalog.pg_views.
 * ==============================================================
 */
@Data
@Entity
@Table(name = "reporting_metadata", schema = "reporting")
public class ReportingMetadata {

    /** 🔑 Nombre único de la vista (sin el prefijo schema) */
    @Id
    @Column(name = "view_name", nullable = false, length = 150)
    private String viewName;

    /** 📝 Descripción corta o comentario de la vista */
    @Column(name = "description", length = 500)
    private String description;

    /** 📦 Lista de columnas de la vista (formato JSON o texto simple) */
    @Column(name = "columns", columnDefinition = "TEXT")
    private String columns;

    /** 🧩 Fecha de registro o sincronización de la vista */
    @Column(name = "fecha_registro")
    private java.time.LocalDateTime fechaRegistro;
}

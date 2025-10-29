package co.assip.erp.shared.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * 🧩 Clase base para entidades auditables en ASSIP-ERP.
 * Se encarga de manejar automáticamente las fechas y usuarios
 * de creación/edición, sin necesidad de triggers en base de datos.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseAudit {

    @Column(name = "fk_seguridad_creacion")
    protected Integer fkSeguridadCreacion;

    @Column(name = "fk_seguridad_edicion")
    protected Integer fkSeguridadEdicion;

    @Column(name = "fecha_creacion")
    protected Timestamp fechaCreacion;

    @Column(name = "fecha_edicion")
    protected Timestamp fechaEdicion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = new Timestamp(System.currentTimeMillis());
        this.fechaEdicion = this.fechaCreacion;

        if (this.fkSeguridadCreacion == null) {
            // 🧠 En producción se tomará del usuario autenticado (JWT)
            this.fkSeguridadCreacion = 1;
        }
        if (this.fkSeguridadEdicion == null) {
            this.fkSeguridadEdicion = this.fkSeguridadCreacion;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaEdicion = new Timestamp(System.currentTimeMillis());

        if (this.fkSeguridadEdicion == null) {
            // 🧠 En producción se tomará del usuario autenticado (JWT)
            this.fkSeguridadEdicion = 1;
        }
    }
}

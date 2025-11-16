package co.assip.erp.sarlaft.domain;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * 📝 Entidad: AlertaGestion
 * ------------------------------------------------------------
 * Registra la gestión oficial realizada sobre una alerta SARLAFT.
 * Cada acción del Oficial queda trazada con auditoría completa.
 */
@Getter
@Setter
@Entity
@Table(name = "alertas_gestion", schema = "general")
public class AlertaGestion extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gestion")
    private Long idGestion;

    /** ID de la alerta a la cual pertenece esta gestión. */
    @Column(name = "id_alerta", nullable = false)
    private Long idAlerta;

    /** Comentario o acción del Oficial SARLAFT. */
    @Column(name = "comentario", nullable = false, columnDefinition = "TEXT")
    private String comentario;

    /** Fecha de la gestión. */
    @Column(name = "fecha", nullable = false)
    private Timestamp fecha;

    @PrePersist
    protected void onCreateGestion() {
        super.onCreate(); // auditoría de BaseAudit
        if (this.fecha == null) {
            this.fecha = new Timestamp(System.currentTimeMillis());
        }
    }
}

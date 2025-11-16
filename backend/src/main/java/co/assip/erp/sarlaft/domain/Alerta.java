package co.assip.erp.sarlaft.domain;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 🚨 Entidad: Alerta SARLAFT
 * ------------------------------------------------------------
 * Representa una alerta generada automáticamente por el motor SARLAFT,
 * almacenada únicamente cuando la severidad es ROJA.
 *
 * Auditoría completa heredada de BaseAudit:
 *  - fk_seguridad_creacion
 *  - fecha_creacion
 *  - fk_seguridad_edicion
 *  - fecha_edicion
 */
@Getter
@Setter
@Entity
@Table(name = "alertas", schema = "general")
public class Alerta extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    @Column(name = "id_datos_personal")
    private Long idDatosPersonal;

    @Column(name = "id_agencia")
    private Integer idAgencia;

    @Column(name = "codigo_modulo", nullable = false, length = 3)
    private String codigoModulo;

    @Column(name = "severidad", nullable = false, length = 10)
    private String severidad;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "accion_sistema", columnDefinition = "TEXT")
    private String accionSistema;

    /** Código de la regla SARLAFT que disparó esta alerta (001, 002, 003…). */
    @Column(name = "codigo_regla", length = 10)
    private String codigoRegla;
}

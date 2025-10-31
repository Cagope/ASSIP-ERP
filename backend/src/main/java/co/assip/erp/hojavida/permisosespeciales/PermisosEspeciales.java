package co.assip.erp.hojavida.permisosespeciales;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

/**
 * 🧩 Entidad: PermisosEspeciales
 * ------------------------------------------------------------
 * Define los medios de comunicación autorizados por cada persona
 * dentro del módulo Hoja de Vida. Cada persona puede registrar un
 * único conjunto de permisos especiales (constraint UNIQUE).
 *
 * Canales: llamadas, SMS, correos, cartas y redes sociales.
 */
@Getter
@Setter
@Entity
@Table(name = "permisos_especiales", schema = "hoja_vida",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permiso_persona", columnNames = "id_datos_personal")
        })
public class PermisosEspeciales extends BaseAudit {

    // ============================================================
    // 🔑 Identificación y relación con persona
    // ============================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso_especial")
    private Integer idPermisoEspecial;

    @Column(name = "id_datos_personal", nullable = false)
    private Integer idDatosPersonal;

    // ============================================================
    // ☎️ Permisos de comunicación
    // ============================================================

    /** ¿Autoriza recibir llamadas telefónicas? */
    @Column(name = "recibe_llamadas", nullable = false)
    private Boolean recibeLlamadas = false;

    @Column(name = "fecha_llamadas")
    private LocalDate fechaLlamadas;

    /** ¿Autoriza recibir mensajes de texto (SMS)? */
    @Column(name = "recibe_msm", nullable = false)
    private Boolean recibeMsm = false;

    @Column(name = "fecha_sms")
    private LocalDate fechaSms;

    /** ¿Autoriza recibir correos electrónicos? */
    @Column(name = "recibe_emails", nullable = false)
    private Boolean recibeEmails = false;

    @Column(name = "fecha_emails")
    private LocalDate fechaEmails;

    /** ¿Autoriza recibir correspondencia física (cartas)? */
    @Column(name = "recibe_cartas", nullable = false)
    private Boolean recibeCartas = false;

    @Column(name = "fecha_cartas")
    private LocalDate fechaCartas;

    /** ¿Autoriza recibir información por redes sociales? */
    @Column(name = "recibe_redes_sociales", nullable = false)
    private Boolean recibeRedesSociales = false;

    @Column(name = "fecha_redes")
    private LocalDate fechaRedes;

    // ============================================================
    // 🕓 Auditoría heredada de BaseAudit
    // ------------------------------------------------------------
    // fkSeguridadCreacion, fkSeguridadEdicion,
    // fechaCreacion, fechaEdicion
    // ============================================================

}

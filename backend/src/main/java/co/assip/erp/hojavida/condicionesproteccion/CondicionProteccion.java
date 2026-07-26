package co.assip.erp.hojavida.condicionesproteccion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad — Condiciones de Protección
 * ------------------------------------------------------------
 * Representa las condiciones especiales, constitucionales
 * y poblacionales asociadas a una persona.
 *
 * Tabla:
 * hoja_vida.condiciones_proteccion
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "condiciones_proteccion",
        schema = "hoja_vida"
)
public class CondicionProteccion {

    // =========================================================
    // Identificación
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_condicion_proteccion")
    private Long idCondicionProteccion;

    @Column(
            name = "id_datos_personal",
            nullable = false,
            unique = true
    )
    private Integer idDatosPersonal;

    // =========================================================
    // Condiciones de protección
    // =========================================================

    /**
     * Indica si la persona administra recursos públicos.
     */
    @Column(
            name = "administra_recursos_publicos",
            nullable = false
    )
    private boolean administraRecursosPublicos = false;

    /**
     * Pertenece a un grupo de protección especial
     * constitucional, como niños, niñas, adolescentes
     * u otros grupos especiales.
     */
    @Column(
            name = "grupo_proteccion_especial_constitucional",
            nullable = false
    )
    private boolean grupoProteccionEspecialConstitucional = false;

    /**
     * Persona mayor de 60 años.
     */
    @Column(
            name = "persona_mayor_60_anos",
            nullable = false
    )
    private boolean personaMayor60Anos = false;

    /**
     * Pertenece al grupo de personas con discapacidad física.
     */
    @Column(
            name = "discapacidad_fisica",
            nullable = false
    )
    private boolean discapacidadFisica = false;

    /**
     * Persona víctima del conflicto armado.
     */
    @Column(
            name = "victima_conflicto_armado",
            nullable = false
    )
    private boolean victimaConflictoArmado = false;

    /**
     * Persona en condición de pobreza extrema.
     */
    @Column(
            name = "pobreza_extrema",
            nullable = false
    )
    private boolean pobrezaExtrema = false;

    /**
     * Pertenece al grupo de población indígena.
     */
    @Column(
            name = "poblacion_indigena",
            nullable = false
    )
    private boolean poblacionIndigena = false;

    /**
     * Pertenece al grupo de población afrodescendiente.
     */
    @Column(
            name = "poblacion_afrodescendiente",
            nullable = false
    )
    private boolean poblacionAfrodescendiente = false;

    /**
     * Pertenece al grupo de población LGBTIQ+.
     */
    @Column(
            name = "poblacion_lgbtiq_mas",
            nullable = false
    )
    private boolean poblacionLgbtiqMas = false;

    /**
     * Pertenece a un grupo de protección constitucional.
     */
    @Column(
            name = "pertenece_grupo_proteccion_constitucional",
            nullable = false
    )
    private boolean perteneceGrupoProteccionConstitucional = false;

    // =========================================================
    // Observaciones
    // =========================================================

    @Column(
            name = "observaciones",
            length = 500
    )
    private String observaciones;

    // =========================================================
    // Auditoría
    // =========================================================

    @Column(
            name = "fk_seguridad_creacion",
            nullable = false,
            updatable = false
    )
    private Integer fkSeguridadCreacion;

    @Column(
            name = "fecha_creacion",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaCreacion;

    @Column(
            name = "fk_seguridad_edicion",
            nullable = false
    )
    private Integer fkSeguridadEdicion;

    @Column(
            name = "fecha_edicion",
            nullable = false
    )
    private LocalDateTime fechaEdicion;

    // =========================================================
    // Ciclo de vida JPA
    // =========================================================

    @PrePersist
    protected void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();

        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }

        if (fechaEdicion == null) {
            fechaEdicion = ahora;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        fechaEdicion = LocalDateTime.now();
    }
}
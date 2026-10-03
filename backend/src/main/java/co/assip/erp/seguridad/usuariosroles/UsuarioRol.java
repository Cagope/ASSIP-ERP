package co.assip.erp.seguridad.usuariosroles;

import co.assip.erp.seguridad.roles.Rol;
import co.assip.erp.seguridad.usuarios.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa la asignación de un rol a un usuario.
 *
 * Un usuario puede tener múltiples roles y un rol puede estar
 * asignado a múltiples usuarios.
 *
 * La asignación conserva su periodo de vigencia y su estado,
 * permitiendo mantener el histórico cuando un rol es retirado
 * o posteriormente asignado nuevamente.
 */
@Entity
@Table(name = "usuarios_roles", schema = "seguridad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRol {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "usuarios_roles_id_usuario_rol_seq"
    )
    @SequenceGenerator(
            name = "usuarios_roles_id_usuario_rol_seq",
            sequenceName = "seguridad.usuarios_roles_id_usuario_rol_seq",
            allocationSize = 1
    )
    @Column(name = "id_usuario_rol")
    private Long idUsuarioRol;

    // =========================================================
    // Usuario y rol
    // =========================================================

    /**
     * Usuario al cual se encuentra asignado el rol.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * Rol asignado al usuario.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    // =========================================================
    // Vigencia de la asignación
    // =========================================================

    /**
     * Fecha desde la cual la asignación del rol tiene vigencia.
     */
    @Column(name = "fecha_desde", nullable = false)
    private LocalDateTime fechaDesde;

    /**
     * Fecha hasta la cual tuvo vigencia la asignación.
     *
     * Permanece en NULL mientras la asignación no tenga
     * una fecha de finalización.
     */
    @Column(name = "fecha_hasta")
    private LocalDateTime fechaHasta;

    /**
     * Indica si la asignación se encuentra habilitada.
     */
    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // =========================================================
    // Auditoría
    // =========================================================

    /**
     * Usuario que realizó originalmente la asignación.
     *
     * Puede ser NULL para información histórica migrada cuyo
     * usuario creador original no se conoce.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_seguridad_creacion")
    private Usuario usuarioCreacion;

    /**
     * Fecha de creación de la asignación.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Usuario que realizó la última modificación.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_seguridad_edicion")
    private Usuario usuarioEdicion;

    /**
     * Fecha de la última modificación.
     */
    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;
}
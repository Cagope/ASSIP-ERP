package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.seguridad.usuarios.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa un token de seguridad asociado a un usuario.
 *
 * Se utiliza para procesos que requieren validar la identidad
 * del propietario de la cuenta, como la activación inicial
 * y la recuperación de contraseña.
 *
 * El código enviado al usuario nunca se almacena directamente.
 * Únicamente se conserva su representación protegida.
 */
@Entity
@Table(name = "usuarios_tokens", schema = "seguridad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioToken {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "usuarios_tokens_id_usuario_token_seq"
    )
    @SequenceGenerator(
            name = "usuarios_tokens_id_usuario_token_seq",
            sequenceName = "seguridad.usuarios_tokens_id_usuario_token_seq",
            allocationSize = 1
    )
    @Column(name = "id_usuario_token")
    private Long idUsuarioToken;

    // =========================================================
    // Usuario
    // =========================================================

    /**
     * Usuario propietario del token.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // =========================================================
    // Token
    // =========================================================

    /**
     * Tipo de operación para la cual fue generado el token.
     *
     * Valores actualmente permitidos por PostgreSQL:
     * ACTIVACION y RECUPERACION.
     */
    @Column(name = "tipo_token", nullable = false, length = 30)
    private String tipoToken;

    /**
     * Representación protegida del código enviado al usuario.
     *
     * Nunca contiene el token de seis dígitos en texto plano.
     */
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    /**
     * Momento hasta el cual el token puede ser utilizado.
     */
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    /**
     * Cantidad de intentos fallidos de validación.
     */
    @Builder.Default
    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    /**
     * Indica si el token ya fue utilizado satisfactoriamente.
     */
    @Builder.Default
    @Column(name = "utilizado", nullable = false)
    private Boolean utilizado = false;

    /**
     * Fecha en la que el token fue utilizado satisfactoriamente.
     */
    @Column(name = "fecha_utilizacion")
    private LocalDateTime fechaUtilizacion;

    /**
     * Indica si el token continúa habilitado.
     *
     * Al generar uno nuevo del mismo tipo, el anterior
     * podrá conservarse históricamente con activo = false.
     */
    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // =========================================================
    // Auditoría
    // =========================================================

    /**
     * Usuario responsable de la creación del registro.
     *
     * Puede ser NULL cuando la generación corresponda a una
     * operación automática o no exista un usuario autenticado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_seguridad_creacion")
    private Usuario usuarioCreacion;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Usuario responsable de la última modificación.
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
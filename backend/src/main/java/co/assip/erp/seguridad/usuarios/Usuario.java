package co.assip.erp.seguridad.usuarios;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa un usuario autenticable del ERP.
 *
 * Contiene la información de acceso, estado de la cuenta,
 * seguridad de contraseña, auditoría básica, rol principal
 * y agencia principal del usuario.
 */
@Entity
@Table(name = "usuarios", schema = "seguridad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuarios_id_usuario_seq")
    @SequenceGenerator(
            name = "usuarios_id_usuario_seq",
            sequenceName = "seguridad.usuarios_id_usuario_seq",
            allocationSize = 1
    )
    @Column(name = "id_usuario")
    private Integer idUsuario;

    // =========================================================
    // Identificación y autenticación
    // =========================================================

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nombre_completo", length = 200)
    private String nombreCompleto;

    @Column(name = "email", length = 150)
    private String email;

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    // =========================================================
    // Rol y agencia principal
    // =========================================================

    /**
     * Rol funcional principal asignado al usuario.
     * Los permisos efectivos del rol se obtienen mediante
     * seguridad.rol_permisos.
     */
    @Column(name = "id_rol")
    private Integer idRol;

    /**
     * Agencia principal del usuario.
     * Las demás agencias autorizadas se administran mediante
     * seguridad.usuarios_agencias.
     */
    @Column(name = "id_agencia_principal")
    private Integer idAgenciaPrincipal;

    // =========================================================
    // Seguridad de contraseña y bloqueo
    // =========================================================

    @Column(name = "password_expira")
    private LocalDateTime passwordExpira;

    @Builder.Default
    @Column(name = "password_intentos")
    private Integer passwordIntentos = 0;

    @Column(name = "password_ultimo_cambio")
    private LocalDateTime passwordUltimoCambio;

    @Builder.Default
    @Column(name = "requiere_cambio_password")
    private Boolean requiereCambioPassword = false;

    @Builder.Default
    @Column(name = "bloqueado")
    private Boolean bloqueado = false;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    // =========================================================
    // Seguimiento de autenticación
    // =========================================================

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "ultimo_intento_login")
    private LocalDateTime ultimoIntentoLogin;

    @Column(name = "ip_ultimo_login", length = 50)
    private String ipUltimoLogin;

    // =========================================================
    // Recuperación de contraseña
    // =========================================================

    @Column(name = "token_recuperacion", length = 255)
    private String tokenRecuperacion;

    @Column(name = "token_expira")
    private LocalDateTime tokenExpira;

    // =========================================================
    // Auditoría
    // =========================================================

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "usuario_creacion", length = 50)
    private String usuarioCreacion;

    @Column(name = "usuario_actualizacion", length = 50)
    private String usuarioActualizacion;

    @Column(name = "origen_creacion", length = 20)
    private String origenCreacion;
}
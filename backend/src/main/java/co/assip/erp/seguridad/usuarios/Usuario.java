package co.assip.erp.seguridad.usuarios;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa una cuenta de usuario autenticable del ERP.
 *
 * La identidad personal puede relacionarse con
 * hoja_vida.datos_personales mediante idDatosPersonal.
 *
 * Los roles funcionales del usuario se administran mediante
 * seguridad.usuarios_roles.
 *
 * Durante la migración al modelo multirrol se conserva
 * temporalmente idRol por compatibilidad con los procesos
 * existentes que todavía dependen del modelo anterior.
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
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "usuarios_id_usuario_seq"
    )
    @SequenceGenerator(
            name = "usuarios_id_usuario_seq",
            sequenceName = "seguridad.usuarios_id_usuario_seq",
            allocationSize = 1
    )
    @Column(name = "id_usuario")
    private Integer idUsuario;

    // =========================================================
    // Identificación de la persona
    // =========================================================

    /**
     * Persona de hoja de vida asociada a la cuenta.
     *
     * Puede ser NULL para cuentas técnicas o cuentas históricas
     * que todavía no hayan sido relacionadas con una persona.
     */
    @Column(name = "id_datos_personal")
    private Integer idDatosPersonal;

    // =========================================================
    // Identificación y autenticación
    // =========================================================

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Hash de la contraseña definitiva del usuario.
     *
     * Permanece NULL mientras la cuenta se encuentra
     * pendiente de activación.
     *
     * La contraseña es establecida exclusivamente por
     * el propietario de la cuenta.
     */
    @Column(name = "password", length = 255)
    private String password;

    /**
     * Campo heredado utilizado actualmente por diferentes
     * procesos del ERP.
     *
     * La fuente definitiva del nombre de una persona será
     * hoja_vida.datos_personales. Se conserva temporalmente
     * mientras se migran sus consumidores.
     */
    @Column(name = "nombre_completo", length = 200)
    private String nombreCompleto;

    @Column(name = "email", length = 150)
    private String email;

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    // =========================================================
    // Vigencia de la cuenta
    // =========================================================

    /**
     * Fecha desde la cual la cuenta puede utilizarse.
     */
    @Column(name = "fecha_inicio_vigencia")
    private LocalDateTime fechaInicioVigencia;

    /**
     * Fecha hasta la cual la cuenta puede utilizarse.
     *
     * NULL indica que no tiene una fecha administrativa
     * de finalización definida.
     */
    @Column(name = "fecha_fin_vigencia")
    private LocalDateTime fechaFinVigencia;

    // =========================================================
    // Agencia principal
    // =========================================================

    /**
     * Agencia principal del usuario.
     *
     * Las agencias autorizadas se administran mediante
     * seguridad.usuarios_agencias.
     */
    @Column(name = "id_agencia_principal")
    private Integer idAgenciaPrincipal;


    // =========================================================
    // Seguridad de contraseña
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

    // =========================================================
    // Bloqueo de la cuenta
    // =========================================================

    @Builder.Default
    @Column(name = "bloqueado")
    private Boolean bloqueado = false;

    /**
     * Fecha en la que se produjo el bloqueo.
     */
    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    /**
     * Fecha hasta la cual permanece un bloqueo temporal.
     *
     * Permite implementar posteriormente el desbloqueo
     * automático después del periodo configurado.
     */
    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

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
    // Recuperación de contraseña - modelo heredado
    // =========================================================

    /**
     * Campo temporal del mecanismo actual de recuperación.
     *
     * Será retirado cuando activación y recuperación utilicen
     * su estructura independiente de tokens.
     */
    @Column(name = "token_recuperacion", length = 255)
    private String tokenRecuperacion;

    /**
     * Vencimiento del token del mecanismo actual.
     */
    @Column(name = "token_expira")
    private LocalDateTime tokenExpira;

    // =========================================================
    // Auditoría actual
    // =========================================================

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /**
     * Campo heredado de auditoría.
     *
     * Se conserva mientras se migran los consumidores al
     * esquema uniforme de auditoría del ERP.
     */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Campo heredado de auditoría basado en username.
     */
    @Column(name = "usuario_creacion", length = 50)
    private String usuarioCreacion;

    /**
     * Campo heredado de auditoría basado en username.
     */
    @Column(name = "usuario_actualizacion", length = 50)
    private String usuarioActualizacion;

    @Column(name = "origen_creacion", length = 20)
    private String origenCreacion;

    // =========================================================
    // Auditoría normalizada
    // =========================================================

    /**
     * Usuario que creó el registro.
     */
    @Column(name = "fk_seguridad_creacion")
    private Integer fkSeguridadCreacion;

    /**
     * Usuario que realizó la última modificación.
     */
    @Column(name = "fk_seguridad_edicion")
    private Integer fkSeguridadEdicion;

    /**
     * Fecha de la última edición.
     */
    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;
}
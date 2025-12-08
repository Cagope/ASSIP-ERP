package co.assip.erp.seguridad.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", schema = "seguridad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nombre_completo", length = 200)
    private String nombreCompleto;

    @Column(name = "email", length = 150)
    private String email;

    // ✔ DEFAULT CORRECTO PARA @Builder
    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "usuario_creacion", length = 50)
    private String usuarioCreacion;

    @Column(name = "usuario_actualizacion", length = 50)
    private String usuarioActualizacion;

    @Column(name = "password_expira")
    private LocalDateTime passwordExpira;

    @Column(name = "password_intentos")
    @Builder.Default
    private Integer passwordIntentos = 0;

    @Column(name = "password_ultimo_cambio")
    private LocalDateTime passwordUltimoCambio;

    @Column(name = "requiere_cambio_password")
    @Builder.Default
    private Boolean requiereCambioPassword = false;

    @Column(name = "bloqueado")
    @Builder.Default
    private Boolean bloqueado = false;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "ultimo_intento_login")
    private LocalDateTime ultimoIntentoLogin;

    @Column(name = "ip_ultimo_login", length = 50)
    private String ipUltimoLogin;

    @Column(name = "token_recuperacion", length = 255)
    private String tokenRecuperacion;

    @Column(name = "token_expira")
    private LocalDateTime tokenExpira;

    @Column(name = "id_agencia_principal")
    private Integer idAgenciaPrincipal;

    @Column(name = "origen_creacion", length = 20)
    private String origenCreacion;

    // ------------------------------------------------------------------------------------
    // ✔ Opción B: solo idRol, sin relación @ManyToOne
    // ------------------------------------------------------------------------------------
    @Column(name = "id_rol")
    private Integer idRol;
}

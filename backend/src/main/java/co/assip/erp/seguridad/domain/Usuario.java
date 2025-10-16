package co.assip.erp.seguridad.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios", schema = "seguridad")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "username", nullable = false, length = 64, updatable = false)
    private String username;

    // NUEVO: hash de contraseña (no exponer en DTOs)
    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;

    // NUEVO: debe cambiar password en próximo login
    @Column(name = "debe_cambiar_password", nullable = false)
    private boolean debeCambiarPassword = true;

    @Column(name = "is_superuser_seguridad", nullable = false)
    private boolean superuserSeguridad;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "ultimo_login_en")
    private OffsetDateTime ultimoLoginEn;

    // getters/setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isDebeCambiarPassword() { return debeCambiarPassword; }
    public boolean isSuperuserSeguridad() { return superuserSeguridad; }
    public boolean isActivo() { return activo; }
    public OffsetDateTime getUltimoLoginEn() { return ultimoLoginEn; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setDebeCambiarPassword(boolean debeCambiarPassword) { this.debeCambiarPassword = debeCambiarPassword; }
    public void setSuperuserSeguridad(boolean superuserSeguridad) { this.superuserSeguridad = superuserSeguridad; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setUltimoLoginEn(OffsetDateTime ultimoLoginEn) { this.ultimoLoginEn = ultimoLoginEn; }
}

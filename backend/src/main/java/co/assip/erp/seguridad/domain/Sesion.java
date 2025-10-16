package co.assip.erp.seguridad.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sesiones", schema = "seguridad")
public class Sesion {

    @Id
    @Column(name = "session_id", columnDefinition = "uuid")
    private UUID sessionId;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "creado_en")
    private OffsetDateTime creadoEn;

    @Column(name = "expira_en")
    private OffsetDateTime expiraEn;

    @Column(name = "ultimo_uso_en")
    private OffsetDateTime ultimoUsoEn;

    @Column(name = "ultimo_ip", columnDefinition = "inet")
    private String ultimoIp;

    @Column(name = "ultimo_user_agent", columnDefinition = "text")
    private String ultimoUserAgent;

    @Column(name = "revocada", nullable = false)
    private boolean revocada;

    @Column(name = "motivo_revocacion", columnDefinition = "text")
    private String motivoRevocacion;

    // getters/setters
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }
    public OffsetDateTime getExpiraEn() { return expiraEn; }
    public void setExpiraEn(OffsetDateTime expiraEn) { this.expiraEn = expiraEn; }
    public OffsetDateTime getUltimoUsoEn() { return ultimoUsoEn; }
    public void setUltimoUsoEn(OffsetDateTime ultimoUsoEn) { this.ultimoUsoEn = ultimoUsoEn; }
    public String getUltimoIp() { return ultimoIp; }
    public void setUltimoIp(String ultimoIp) { this.ultimoIp = ultimoIp; }
    public String getUltimoUserAgent() { return ultimoUserAgent; }
    public void setUltimoUserAgent(String ultimoUserAgent) { this.ultimoUserAgent = ultimoUserAgent; }
    public boolean isRevocada() { return revocada; }
    public void setRevocada(boolean revocada) { this.revocada = revocada; }
    public String getMotivoRevocacion() { return motivoRevocacion; }
    public void setMotivoRevocacion(String motivoRevocacion) { this.motivoRevocacion = motivoRevocacion; }
}

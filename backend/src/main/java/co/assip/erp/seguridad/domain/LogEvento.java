package co.assip.erp.seguridad.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "log_eventos", schema = "seguridad")
public class LogEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long id;

    @Column(name = "accion", nullable = false, length = 80)
    private String accion;

    @Column(name = "resultado", nullable = false, length = 8)
    private String resultado; // OK / FAIL

    @Column(name = "entidad", length = 50)
    private String entidad;

    @Column(name = "status_code")
    private Integer statusCode;

    // En BD la columna se llama 'username'
    @Column(name = "username", length = 64)
    private String usernameActor;

    // En BD es tipo 'inet'
    @Column(name = "ip", columnDefinition = "inet")
    @JdbcTypeCode(SqlTypes.INET)
    private String ip;

    // En BD es 'text' (no varchar(255))
    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    // En BD es 'jsonb'
    @Column(name = "extra_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String extraJson;

    // --- getters/setters ---
    public Long getId() { return id; }
    public String getAccion() { return accion; }
    public String getResultado() { return resultado; }
    public String getEntidad() { return entidad; }
    public Integer getStatusCode() { return statusCode; }
    public String getUsernameActor() { return usernameActor; }
    public String getIp() { return ip; }
    public String getUserAgent() { return userAgent; }
    public String getExtraJson() { return extraJson; }

    public void setId(Long id) { this.id = id; }
    public void setAccion(String accion) { this.accion = accion; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public void setUsernameActor(String usernameActor) { this.usernameActor = usernameActor; }
    public void setIp(String ip) { this.ip = ip; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setExtraJson(String extraJson) { this.extraJson = extraJson; }
}

package co.assip.erp.seguridad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_evento", schema = "seguridad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Integer idLog;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "modulo", length = 100, nullable = false)
    private String modulo;

    @Column(name = "accion", length = 100, nullable = false)
    private String accion;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    @Column(name = "user_agent")
    private String userAgent;

    // 🔹 Hook automático: si no se define fechaEvento, se coloca antes de persistir
    @PrePersist
    public void prePersist() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}

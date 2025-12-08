package co.assip.erp.seguridad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios_agencias", schema = "seguridad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAgencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_agencia")
    private Integer idUsuarioAgencia;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "id_agencia", nullable = false)
    private Integer idAgencia;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_creacion")
    private Integer fkSeguridadCreacion;
}

package co.assip.erp.seguridad.usuariosagencias;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios_agencias", schema = "seguridad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAgencia {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "usuarios_agencias_id_usuario_agencia_seq"
    )
    @SequenceGenerator(
            name = "usuarios_agencias_id_usuario_agencia_seq",
            sequenceName = "seguridad.usuarios_agencias_id_usuario_agencia_seq",
            allocationSize = 1
    )
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
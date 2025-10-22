package co.assip.erp.seguridad.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permisos", schema = "seguridad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Integer idPermiso;

    @Column(name = "codigo", nullable = false, unique = true, length = 100)
    private String codigo;

    @Column(name = "descripcion")
    private String descripcion;

    // 🔗 Relación con Rol
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    private Rol rol;
}

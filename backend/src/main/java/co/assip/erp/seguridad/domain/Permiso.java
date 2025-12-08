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

    @Builder.Default
    @Column(name = "activo")
    private Boolean activo = true;

    // ✔ MODELO DEFINITIVO (campo plano)
    @Column(name = "id_rol")
    private Integer idRol;
}

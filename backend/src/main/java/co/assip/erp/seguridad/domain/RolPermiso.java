package co.assip.erp.seguridad.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rol_permisos", schema = "seguridad")
@IdClass(RolPermisoId.class)
@Getter
@Setter
public class RolPermiso {

    // 🔑 Parte 1 de la clave compuesta (id_rol)
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    // 🔑 Parte 2 de la clave compuesta (id_permiso)
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;
}

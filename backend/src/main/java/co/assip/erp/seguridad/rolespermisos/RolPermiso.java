package co.assip.erp.seguridad.rolespermisos;

import co.assip.erp.seguridad.permisos.Permiso;
import co.assip.erp.seguridad.roles.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa la asignación de un permiso a un rol.
 *
 * Cada registro corresponde a una relación única entre
 * seguridad.roles y seguridad.permisos, almacenada en
 * seguridad.rol_permisos.
 *
 * La clave primaria está compuesta por id_rol e id_permiso.
 */
@Entity
@Table(name = "rol_permisos", schema = "seguridad")
@IdClass(RolPermisoId.class)
@Getter
@Setter
public class RolPermiso {

    /**
     * Rol al cual se asigna el permiso.
     * Forma parte de la clave primaria compuesta.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    /**
     * Permiso asignado al rol.
     * Forma parte de la clave primaria compuesta.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;
}
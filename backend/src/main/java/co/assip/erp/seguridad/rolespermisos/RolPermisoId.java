package co.assip.erp.seguridad.rolespermisos;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta de la relación Rol-Permiso.
 *
 * Representa las columnas:
 * - id_rol
 * - id_permiso
 *
 * Los nombres de las propiedades deben corresponder con los
 * atributos identificadores definidos en RolPermiso.
 */
public class RolPermisoId implements Serializable {

    private Integer rol;
    private Integer permiso;

    /**
     * Constructor requerido por JPA.
     */
    public RolPermisoId() {
    }

    /**
     * Construye la clave compuesta de una asignación Rol-Permiso.
     *
     * @param rol identificador del rol
     * @param permiso identificador del permiso
     */
    public RolPermisoId(Integer rol, Integer permiso) {
        this.rol = rol;
        this.permiso = permiso;
    }

    public Integer getRol() {
        return rol;
    }

    public void setRol(Integer rol) {
        this.rol = rol;
    }

    public Integer getPermiso() {
        return permiso;
    }

    public void setPermiso(Integer permiso) {
        this.permiso = permiso;
    }

    /**
     * Dos claves son iguales cuando contienen el mismo rol
     * y el mismo permiso.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RolPermisoId that = (RolPermisoId) o;

        return Objects.equals(rol, that.rol)
                && Objects.equals(permiso, that.permiso);
    }

    /**
     * Genera el hash utilizando ambas partes de la clave.
     */
    @Override
    public int hashCode() {
        return Objects.hash(rol, permiso);
    }
}
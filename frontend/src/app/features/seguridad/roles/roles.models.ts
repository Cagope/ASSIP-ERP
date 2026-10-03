/**
 * Rol funcional del sistema.
 *
 * Un usuario puede tener uno o varios roles activos
 * mediante seguridad.usuarios_roles.
 */
export interface Rol {
  idRol: number;
  nombreRol: string;
  activo?: boolean;
}

/**
 * Permiso actualmente asignado a un rol.
 *
 * Esta representación se utiliza al consultar
 * seguridad.rol_permisos.
 */
export interface PermisoRol {
  idPermiso: number;
  codigo: string;
  descripcion?: string | null;
  activo?: boolean;
}

/**
 * Permiso funcional disponible en ASSIP ERP.
 *
 * Un permiso representa una operación que puede ser
 * autorizada a uno o varios roles.
 */
export interface Permiso {
  idPermiso: number;
  codigo: string;
  descripcion: string | null;
  activo: boolean;
}

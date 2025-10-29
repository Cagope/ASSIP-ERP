/**
 * 🧩 Entidad base del ERP ASSIP
 * Incluye los campos comunes de auditoría presentes en todas las tablas.
 */
export interface EntidadBase {
  fkSeguridadCreacion?: number;
  fechaCreacion?: string;
  fkSeguridadEdicion?: number;
  fechaEdicion?: string;
}

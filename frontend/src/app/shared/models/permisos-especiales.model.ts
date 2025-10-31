/**
 * 🧾 Modelo — Permiso Especial
 * ------------------------------------------------------------
 * Representa los permisos otorgados por una persona para
 * recibir comunicaciones por distintos medios.
 * Usado en el módulo Hoja de Vida → Permisos Especiales.
 */
export interface PermisoEspecial {
  idPermisoEspecial?: number;
  idDatosPersonal?: number;

  // 🟩 Permisos de contacto
  recibeLlamadas?: boolean;
  fechaLlamadas?: string | null;

  recibeSms?: boolean;
  fechaSms?: string | null;

  recibeEmails?: boolean;
  fechaEmails?: string | null;

  recibeCartas?: boolean;
  fechaCartas?: string | null;

  recibeRedesSociales?: boolean;
  fechaRedesSociales?: string | null;

  // 🕓 Auditoría
  fechaCreacion?: string | null;
  fechaEdicion?: string | null;
}

/**
 * 👥 Modelo — Referencia Personal
 * -------------------------------------------------------------------
 * Representa una referencia personal asociada a una persona (Hoja de Vida).
 * Basado en la entidad del backend: hoja_vida.referencias_personales
 */
export interface ReferenciaPersonal {
  idReferenciaPersonal?: number;           // Identificador principal
  idDatosPersonal: number;                  // ID de la persona (FK hacia datos_personales)
  nombreReferenciaPersonal: string;         // Nombre completo de la referencia
  direccionReferenciaPersonal: string;      // Dirección de contacto
  idDepartamento?: number;                  // Departamento (catálogo)
  idCiudad?: number;                        // Ciudad (catálogo)
  telefonoReferenciaPersonal?: string;      // Teléfono fijo (7 dígitos)
  celularReferenciaPersonal?: string;       // Celular (10 dígitos)
  fkSeguridadCreacion: number;              // Usuario que creó el registro
  fechaCreacion?: Date;                     // Fecha de creación
  fkSeguridadEdicion: number;               // Usuario que editó el registro
  fechaEdicion?: Date;                      // Fecha de última edición
}

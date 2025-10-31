/**
 * 👨‍👩‍👧‍👦 Modelo — Datos Familiar
 * Representa la información del grupo familiar de una persona dentro del módulo Hoja de Vida.
 * Incluye el idDatosPersonal para mantener la relación con DatosPersonales.
 */
export interface DatosFamiliar {
  idDatosFamiliares?: number;          // Identificador principal
  idDatosPersonal: number;             // ID de la persona titular (FK hoja_vida.datos_personales)
  codigoParentesco?: string;           // Código del parentesco (FK catálogo)
  nombreDatosFamiliar: string;         // Nombre completo del familiar
  documentoDatosFamiliar: string;      // Documento del familiar
  telefonoDatosFamiliar?: string;      // Teléfono fijo (7 dígitos)
  celularDatosFamiliar?: string;       // Celular (10 dígitos)
  direccionDatosFamiliar: string;      // Dirección del familiar
  idDepartamento?: number;             // FK hacia catálogo de departamentos
  idCiudad?: number;                   // FK hacia catálogo de ciudades
  ingresosDatosFamiliar?: number;      // Ingresos mensuales
  egresosDatosFamiliar?: number;       // Egresos mensuales
  referenciaFamiliar?: boolean;        // Marca si es referencia familiar
  fkSeguridadCreacion: number;         // Usuario que creó el registro
  fechaCreacion?: Date;                // Fecha de creación
  fkSeguridadEdicion: number;          // Usuario que editó el registro
  fechaEdicion?: Date;                 // Fecha de edición

  // 🔹 Campos extendidos (opcionalmente añadidos desde la vista de lista/exportación)
  documento?: string;                  // Documento del titular (DatosPersonales)
  nombrePersona?: string;              // Nombre completo del titular (DatosPersonales)
}

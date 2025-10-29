import { EntidadBase } from './entidad-base.model';

/**
 * 👤 Entidad asociada a personas naturales o jurídicas.
 * Contiene solo la FK común usada por Hoja de Vida, Laborales, Familiares, etc.
 */
export interface ConDatosPersonales {
  idDatosPersonal?: number;
}

/**
 * 🧾 Representación básica de una persona en el ERP.
 * Se amplía en módulos como DatosPersonales o Directivos.
 */
export interface PersonaBase extends EntidadBase, ConDatosPersonales {
  tipoDocumento?: string;
  documento?: string;
  nombres?: string;
  primerApellido?: string;
  segundoApellido?: string;
}

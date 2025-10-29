/**
 * 💼 Modelo Laboral (Hoja de Vida)
 * Representa la información laboral y contractual asociada a una persona.
 */
export interface Laboral {
  idLaboral?: number;
  idDatosPersonal: number;
  nombreEmpresa: string;
  direccion: string;
  idPais?: number;
  idDepartamento?: number;
  idCiudad?: number;
  telefonoEmpresa?: string;
  celularEmpresa?: string;
  correoEmpresa?: string;
  codigoTipoEmpresa?: string;
  empleadoEntidad?: boolean;
  codigoTipoContrato?: string;
  codigoJornada?: string;
  nombreContacto?: string;
  celularContacto?: string | null;
  fechaVinculacion?: string;
  fkSeguridadCreacion?: number;
  fechaCreacion?: string;
  fkSeguridadEdicion?: number;
  fechaEdicion?: string;
}

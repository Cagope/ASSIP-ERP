/**
 * 🎯 DTO — Apertura manual de cuentas desde Hoja de Vida
 * Este modelo sincroniza 1:1 con el backend (AperturaCuentasEntradaDTO)
 */
export interface AperturaCuentaDTO {

  /** ID del asociado (hoja de vida) */
  idDatosPersonal: number;

  /** ID de la forma de ahorro seleccionada */
  idFormaAhorro: number;

  /** “S” o “N” — Aplicación de GMF */
  gmf: string;

  /** true/false — Aplicación de retención */
  retencion: boolean;

  /** Datos de apoderado (opcionales) */
  documentoApoderado?: string | null;
  nombreApoderado?: string | null;
  telefonoApoderado?: string | null;
  celularApoderado?: string | null;

  /** Auditoría — ID del usuario autenticado */
  usuarioId: number;
}

/**
 * 🛠 Helper para construir DTO limpio desde formulario Angular
 */
export function buildAperturaCuentaDTO(
  idDatosPersonal: number,
  formValue: any,
  usuarioId: number
): AperturaCuentaDTO {

  return {
    idDatosPersonal,

    idFormaAhorro: Number(formValue.idFormaAhorro),

    gmf: formValue.gmf,
    retencion: !!formValue.retencion,

    documentoApoderado:
      formValue.documentoApoderado?.trim() || null,

    nombreApoderado:
      formValue.nombreApoderado?.trim() || null,

    telefonoApoderado:
      formValue.telefonoApoderado?.trim() || null,

    celularApoderado:
      formValue.celularApoderado?.trim() || null,

    usuarioId
  };
}

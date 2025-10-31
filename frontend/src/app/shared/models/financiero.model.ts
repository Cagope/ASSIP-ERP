/**
 * 💰 Modelo Financiero (Hoja de Vida)
 * ------------------------------------------------------------
 * Representa la información económica y patrimonial de un afiliado,
 * asociada a su registro en `datos_personales`.
 */
export interface Financiero {
  idFinanciero?: number;
  idDatosPersonal: number; // Relación con datos_personales

  // 🟩 Ingresos
  valorSalario?: number;
  valorPension?: number;
  ingresosArriendo?: number;
  ingresosComisiones?: number;
  otrosIngresos?: number;
  comentarioOtrosIngresos?: string;
  origenFondos?: string;

  // 🟥 Egresos
  egresosFamiliares?: number;
  egresosArriendo?: number;
  egresosCredito?: number;
  otrosEgresos?: number;
  comentarioOtrosEgresos?: string;

  // 🟨 Patrimonio
  totalActivos?: number;
  totalPasivos?: number;
  deudaRelacionFinanciera?: number;
  relacionFinanciera?: string;

  // 🕓 Auditoría
  fkSeguridadCreacion?: number;
  fechaCreacion?: string | Date | null;
  fkSeguridadActualizacion?: number;
  fechaActualizacion?: string | Date | null;
}

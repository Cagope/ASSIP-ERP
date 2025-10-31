/**
 * 🧾 Modelo SARLAFT (Hoja de Vida)
 * Representa la información relacionada con la prevención de lavado de activos y financiación del terrorismo
 * asociada a una persona.
 */
export interface Sarlaft {
  idSarlaft?: number;
  idDatosPersonal: number;

  // 🟩 Exoneración UIAF
  exoneracionUiaf?: boolean;
  fechaExoneracion?: string;

  // 🟦 Asociado PEPS
  asociadoPeps?: boolean;
  tipoPeps?: string;
  observacionesPeps?: string;
  fechaInicialPeps?: string;
  fechaFinalPeps?: string;

  // 🟨 Familiares PEPS
  familiaPeps?: boolean;
  tipoFamiliaPeps?: string;
  codigoParentesco?: string;
  cedulaFamiliaPeps?: string;
  nombreFamiliaPeps?: string;

  // 🟪 Moneda extranjera
  monedaExtranjera?: boolean;
  observacionMonedaExtranjera?: string;

  // 🟧 Cuenta en el extranjero
  cuentaExtranjero?: boolean;
  tipoMonedaExtranjera?: string;
  numeroCuentaExtranjero?: string;
  nombreBancoExtranjero?: string;
  ciudadCuentaExtranjero?: string;
  paisCuentaExtranjero?: string;

  // ⚙️ Auditoría
  fkSeguridadCreacion?: number;
  fechaCreacion?: string;
  fkSeguridadEdicion?: number;
  fechaEdicion?: string;
}

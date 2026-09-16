// =========================================================
// RESUMEN FINANCIERO POR DEUDOR
// =========================================================

export interface SolicitudFinanciero {
  idSolicitudDeudorFinanciero: number | null;
  idSolicitudDeudor: number;
  idSolicitudCredito: number;

  idDatosPersonal: number;
  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;

  tipoDeudor: string | null;
  ordenDeudor: number;

  tipoPersona: string | null;
  fechaFotografia: string | null;

  ingresosTotalesNatural: number | null;
  egresosTotalesNatural: number | null;

  ingresosTotalesJuridica: number | null;
  egresosTotalesJuridica: number | null;

  totalActivos: number | null;
  totalPasivos: number | null;
  patrimonioTotal: number | null;

  activo: boolean | null;
}


// =========================================================
// DETALLE FINANCIERO
// =========================================================

export interface SolicitudFinancieroDetalle {

  // ---------------------------------------------------------
  // Identificación
  // ---------------------------------------------------------

  idSolicitudDeudorFinanciero: number | null;
  idSolicitudDeudor: number;
  idSolicitudCredito: number;

  idDatosPersonal: number;

  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;

  tipoDeudor: string | null;
  ordenDeudor: number;

  tipoPersona: string | null;
  fechaFotografia: string | null;


  // ---------------------------------------------------------
  // Actividad económica
  // ---------------------------------------------------------

  codigoOcupacion: string | null;
  codigoSectorEconomico: string | null;
  codigoActividadSes: string | null;
  codigoActividadDian: string | null;


  // ---------------------------------------------------------
  // Persona natural - ingresos
  // ---------------------------------------------------------

  valorSalario: number | null;
  valorPension: number | null;
  ingresoIndependiente: number | null;
  ingresosArriendo: number | null;
  ingresosComisiones: number | null;
  otrosIngresos: number | null;

  comentarioOtrosIngresos: string | null;

  ingresosTotalesNatural: number | null;


  // ---------------------------------------------------------
  // Persona natural - egresos
  // ---------------------------------------------------------

  egresosFamiliares: number | null;
  egresosArriendo: number | null;
  egresosCredito: number | null;
  otrosEgresos: number | null;

  comentarioOtrosEgresos: string | null;

  egresosTotalesNatural: number | null;


  // ---------------------------------------------------------
  // Declaración de renta
  // ---------------------------------------------------------

  declaraRenta: boolean | null;
  anioDeclaracion: number | null;
  fechaPresentacionDeclaracion: string | null;


  // ---------------------------------------------------------
  // Persona jurídica - ingresos
  // ---------------------------------------------------------

  ingresosOperacionales: number | null;
  ingresosNoOperacionales: number | null;

  ingresosTotalesJuridica: number | null;


  // ---------------------------------------------------------
  // Persona jurídica - egresos
  // ---------------------------------------------------------

  costos: number | null;
  gastosOperacionales: number | null;
  gastosFinancieros: number | null;
  otrosGastos: number | null;

  egresosTotalesJuridica: number | null;


  // ---------------------------------------------------------
  // Balance
  // ---------------------------------------------------------

  activoCorriente: number | null;
  pasivoCorriente: number | null;

  utilidadOperacional: number | null;
  utilidadNeta: number | null;

  totalActivos: number | null;
  totalPasivos: number | null;
  patrimonioTotal: number | null;


  // ---------------------------------------------------------
  // Información complementaria
  // ---------------------------------------------------------

  origenFondos: string | null;
  relacionFinanciera: string | null;
  deudaRelacionFinanciera: number | null;


  // ---------------------------------------------------------
  // Sincronización con Hoja de Vida
  // ---------------------------------------------------------

  sincronizadoHojaVida: boolean | null;


  // ---------------------------------------------------------
  // Control
  // ---------------------------------------------------------

  activo: boolean | null;

  fechaCreacion: string | null;
  fechaEdicion: string | null;
}


// =========================================================
// REQUEST GUARDAR / ACTUALIZAR INFORMACIÓN FINANCIERA
// =========================================================

export interface SolicitudFinancieroGuardarRequest {

  // ---------------------------------------------------------
  // Deudor
  // ---------------------------------------------------------

  idSolicitudDeudor: number;


  // ---------------------------------------------------------
  // Actividad económica
  // ---------------------------------------------------------

  codigoOcupacion: string | null;
  codigoSectorEconomico: string | null;
  codigoActividadSes: string | null;
  codigoActividadDian: string | null;


  // ---------------------------------------------------------
  // Persona natural - ingresos
  // ---------------------------------------------------------

  valorSalario: number | null;
  valorPension: number | null;
  ingresoIndependiente: number | null;
  ingresosArriendo: number | null;
  ingresosComisiones: number | null;
  otrosIngresos: number | null;

  comentarioOtrosIngresos: string | null;


  // ---------------------------------------------------------
  // Persona natural - egresos
  // ---------------------------------------------------------

  egresosFamiliares: number | null;
  egresosArriendo: number | null;
  egresosCredito: number | null;
  otrosEgresos: number | null;

  comentarioOtrosEgresos: string | null;


  // ---------------------------------------------------------
  // Declaración de renta
  // ---------------------------------------------------------

  declaraRenta: boolean | null;
  anioDeclaracion: number | null;
  fechaPresentacionDeclaracion: string | null;


  // ---------------------------------------------------------
  // Persona jurídica - ingresos
  // ---------------------------------------------------------

  ingresosOperacionales: number | null;
  ingresosNoOperacionales: number | null;


  // ---------------------------------------------------------
  // Persona jurídica - egresos
  // ---------------------------------------------------------

  costos: number | null;
  gastosOperacionales: number | null;
  gastosFinancieros: number | null;
  otrosGastos: number | null;


  // ---------------------------------------------------------
  // Balance
  // ---------------------------------------------------------

  activoCorriente: number | null;
  pasivoCorriente: number | null;

  utilidadOperacional: number | null;
  utilidadNeta: number | null;

  totalActivos: number | null;
  totalPasivos: number | null;


  // ---------------------------------------------------------
  // Información complementaria
  // ---------------------------------------------------------

  origenFondos: string | null;
  relacionFinanciera: string | null;
  deudaRelacionFinanciera: number | null;
}

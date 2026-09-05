export interface GarantiasCoberturaResumen {
  idCierreCartera: number;
  fechaCorte: string;
  cantidadBienes: number;
  valorTotalBienes: number;
  bienesSuficientes: number;
  bienesInsuficientes: number;
  margenTotalBienes: number;
  deficitTotalBienes: number;
  cantidadCreditosConBien: number;
  creditosConMultiplesBienes: number;
  saldoCreditosConBien: number;
  valorGarantiasAsignadas: number;
  coberturaEfectivaCreditos: number;
  exposicionNoCubierta: number;
  creditosCoberturaSuficiente: number;
  creditosCoberturaInsuficiente: number;
  porcentajeCoberturaEfectiva: number;
  porcentajeExposicionNoCubierta: number;
}

export interface GarantiasCoberturaBien {
  idCierreCartera: number;
  fechaCorte: string;
  idBien: number;
  tipoBien: string;
  descripcionBien: string;
  identificacionBien: string;
  valorBienFechaCorte: number;
  cantidadCreditosBien: number;
  saldoTotalCreditosBien: number;
  valorGarantiaDistribuida: number;
  coberturaEfectivaBien: number;
  porcentajeCoberturaBien: number;
  margenCobertura: number;
  deficitCobertura: number;
  estadoCobertura: string;
}

export interface GarantiasCoberturaCredito {
  idCierreCartera: number;
  fechaCorte: string;
  idCarteraCredito: number;
  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;
  pagareCartera: string;
  idAgencia: number;
  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;
  saldoCredito: number;
  cantidadBienes: number;
  valorGarantiasAsignadas: number;
  coberturaEfectivaCredito: number;
  exposicionNoCubierta: number;
  porcentajeCoberturaCredito: number;
  estadoCoberturaCredito: string;
  diasMora: number;
  edadContableResultado: string;
  deterioroCapital: number;
  deterioroIntereses: number;
  deterioroOtros: number;
  deterioroTotal: number;
  cantidadCodeudoresActual: number;
  tieneCodeudorActual: boolean;
}

export interface GarantiasCoberturaDetalle {
  idCierreCartera: number;
  fechaCorte: string;
  idBien: number;
  tipoBien: string;
  descripcionBien: string;
  identificacionBien: string;
  valorBienFechaCorte: number;
  cantidadCreditosBien: number;
  saldoTotalCreditosBien: number;
  coberturaEfectivaBien: number;
  porcentajeCoberturaBien: number;
  margenCobertura: number;
  deficitCobertura: number;
  estadoCobertura: string;
  idCierreCarteraCredito: number;
  idCarteraCredito: number;
  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;
  pagareCartera: string;
  idAgencia: number;
  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;
  codigoClasificacionCredito: string;
  descripcionClasificacionCredito: string;
  codigoGarantiaCredito: string;
  descripcionGarantiaCredito: string;
  codigoDestinoEconomico: string;
  descripcionDestinoEconomico: string;
  saldoCredito: number;
  porcentajeCreditoBien: number;
  valorGarantiaCreditoBien: number;
  diasMora: number;
  edadContableResultado: string;
  deterioroCapital: number;
  deterioroIntereses: number;
  deterioroOtros: number;
  deterioroTotal: number;
  cantidadCodeudoresActual: number;
  tieneCodeudorActual: boolean;
  alertaCobertura: string;
}

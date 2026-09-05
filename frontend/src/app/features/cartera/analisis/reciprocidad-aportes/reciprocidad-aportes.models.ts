export interface ReciprocidadAportesResumen {
  idCierreCartera: number;
  fechaCorte: string;
  cantidadPersonas: number;
  cantidadCreditos: number;
  saldoCartera: number;
  saldoAportes: number;
  porcentajeReciprocidadGlobal: number;
  apalancamientoGlobal: number;
  exposicionNetaAportes: number;
  excedenteAportes: number;
  personasSinAportes: number;
  personasAportesMenoresCartera: number;
  personasAportesCubrenCartera: number;
  personasReciprocidadMenor5: number;
  personasReciprocidad5_10: number;
  personasReciprocidad10_20: number;
  personasReciprocidad20_50: number;
  personasReciprocidad50_100: number;
  personasReciprocidad100Mas: number;
}

export interface ReciprocidadAportesPersona {
  idCierreCartera: number;
  fechaCorte: string;
  idDatosPersonal: number;
  tipoDocumento: string | null;
  documento: string;
  nombreCompleto: string;
  cantidadCreditos: number;
  saldoCartera: number;
  saldoAportes: number;
  aportesDistribuidos: number;
  diferenciaDistribucionAportes: number;
  porcentajeReciprocidad: number;
  apalancamiento: number;
  exposicionNetaAportes: number;
  excedenteAportes: number;
  aportesCubrenCartera: boolean;
  rangoReciprocidad: string;
}

export interface ReciprocidadAportesDetalle {
  idCierreCartera: number;
  fechaCorte: string;

  idDatosPersonal: number;
  tipoDocumento: string | null;
  documento: string;
  nombreCompleto: string;

  cantidadCreditosPersona: number;
  saldoCarteraPersona: number;
  saldoAportesPersona: number;
  porcentajeReciprocidadPersona: number;
  apalancamientoPersona: number;

  idCierreCarteraCredito: number;
  idCarteraCredito: number;
  pagareCartera: string;

  idAgencia: number;
  idLineaCredito: number;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  codigoClasificacionCredito: string | null;
  descripcionClasificacionCredito: string | null;

  codigoDestinoEconomico: string | null;
  descripcionDestinoEconomico: string | null;

  fechaDesembolso: string | null;
  valorInicialCredito: number;
  valorDesembolsado: number;
  saldoCreditoFechaCorte: number;

  porcentajeAportesCredito: number;
  valorAportesCredito: number;
  exposicionNetaCredito: number;
  excedenteAportesCredito: number;

  diasMora: number;
  edadContableResultado: string | null;

  deterioroCapital: number;
  deterioroIntereses: number;
  deterioroOtros: number;
  deterioroTotal: number;
}

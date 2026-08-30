// =========================================================
// VECTOR DE COMPORTAMIENTO - RESUMEN
//
// 1 fila = 1 crédito activo con saldo actual > 0.
//
// Consolida:
// - identificación;
// - contacto;
// - período observado;
// - comportamiento histórico;
// - originación;
// - saldos;
// - severidad;
// - edades;
// - pérdida esperada;
// - deterioros;
// - estado actual del maestro.
// =========================================================

export interface VectorComportamientoResumen {

  // =======================================================
  // IDENTIFICACIÓN
  // =======================================================

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string;

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string;

  nombreCompleto: string;


  // =======================================================
  // CONTACTO
  // =======================================================

  telefono: string | null;

  celular: string | null;

  correo: string | null;


  // =======================================================
  // PERÍODO OBSERVADO
  // =======================================================

  primerCorte: string | null;

  ultimoCorte: string | null;

  cantidadCortesObservados: number;


  // =======================================================
  // COMPORTAMIENTO DE MORA
  // =======================================================

  moraUltimoCorte: number;

  moraMaxima: number;

  cantidadCortesAlDia: number;

  cantidadCortesConMora: number;

  cantidadMora1_30: number;

  cantidadMora31_60: number;

  cantidadMora61_90: number;

  cantidadMora91_120: number;

  cantidadMora121_150: number;

  cantidadMora151_180: number;

  cantidadMora181_360: number;

  cantidadMoraMayor360: number;

  pbbMora: number;


  // =======================================================
  // ORIGINACIÓN
  // =======================================================

  fechaDesembolso: string | null;

  valorInicialCredito: number;

  valorDesembolsado: number;


  // =======================================================
  // SALDOS Y SEVERIDAD
  // =======================================================

  saldoPrimerCorte: number;

  saldoUltimoCorte: number;

  variacionSaldoPeriodo: number;

  saldoActualMaestro: number;

  severidad: number | null;

  rangoSeveridad: string;


  // =======================================================
  // ESTADO AL ÚLTIMO CORTE
  // =======================================================

  codigoEstadoCartera: string | null;

  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;

  descripcionEstadoJuridico: string | null;

  codigoClasificacionCredito: string | null;

  descripcionClasificacionCredito: string | null;


  // =======================================================
  // EDADES AL ÚLTIMO CORTE
  // =======================================================

  edadRiesgoInicialResultado: string | null;

  edadDeMoraResultado: string | null;

  edadDeRiesgoResultado: string | null;

  edadDePeResultado: string | null;

  edadDeHomologacionResultado: string | null;

  edadContableResultado: string | null;


  // =======================================================
  // MODELO / PÉRDIDA ESPERADA
  // =======================================================

  codigoMetodoCalculo: string | null;

  vea: number | null;

  pi: number | null;

  pdi: number | null;

  perdidaEsperada: number | null;


  // =======================================================
  // DETERIOROS
  // =======================================================

  deterioroCapital: number | null;

  deterioroIntereses: number | null;

  deterioroOtros: number | null;

  deterioroTotal: number | null;


  // =======================================================
  // ESTADO ACTUAL DEL MAESTRO
  // =======================================================

  codigoEstadoCarteraActual: string | null;

  creditoActivoActual: boolean;


  // =======================================================
  // INDICADORES
  // =======================================================

  tuvoMoraPeriodo: boolean;

  estaEnMoraUltimoCorte: boolean;

  tuvoMoraMayor90: boolean;

  tieneHistoria: boolean;


  // =======================================================
  // IDENTIFICADORES HISTÓRICOS
  // =======================================================

  idCierreCartera: number | null;

  idCierreCarteraCredito: number | null;

  idCierreCarteraResultado: number | null;
}


// =========================================================
// VECTOR DE COMPORTAMIENTO - DETALLE
//
// 1 fila = 1 crédito activo + 1 corte histórico.
//
// La población pertenece al Vector Actual porque
// el crédito tiene saldo actual > 0 en el maestro.
//
// Un crédito activo sin historia no genera registros
// artificiales en este detalle.
// =========================================================

export interface VectorComportamientoDetalle {

  // =======================================================
  // IDENTIFICACIÓN
  // =======================================================

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string;

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string;

  nombreCompleto: string;


  // =======================================================
  // CORTE HISTÓRICO
  // =======================================================

  idCierreCartera: number;

  idCierreCarteraCredito: number;

  idCierreCarteraResultado: number;

  fechaCorte: string;

  anioCorte: number;

  mesCorte: number;

  periodoCorte: string;


  // =======================================================
  // ESTADO DEL CRÉDITO EN EL CORTE
  // =======================================================

  codigoEstadoCartera: string | null;

  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;

  descripcionEstadoJuridico: string | null;

  codigoClasificacionCredito: string | null;

  descripcionClasificacionCredito: string | null;


  // =======================================================
  // MORA
  // =======================================================

  diasMora: number;

  rangoMora: string;

  ordenRangoMora: number;

  tieneMora: boolean;


  // =======================================================
  // EDADES
  // =======================================================

  edadRiesgoInicialResultado: string | null;

  edadDeMoraResultado: string | null;

  edadDeRiesgoResultado: string | null;

  edadDePeResultado: string | null;

  edadDeHomologacionResultado: string | null;

  edadContableResultado: string | null;


  // =======================================================
  // ORIGINACIÓN
  // =======================================================

  valorInicialCredito: number;

  valorDesembolsado: number;


  // =======================================================
  // SALDOS
  // =======================================================

  saldoActualFotografia: number;

  saldoActualResultado: number;

  saldoCreditoFechaCorte: number;


  // =======================================================
  // MODELO / PÉRDIDA ESPERADA
  // =======================================================

  codigoMetodoCalculo: string | null;

  vea: number | null;

  pi: number | null;

  pdi: number | null;

  perdidaEsperada: number | null;


  // =======================================================
  // DETERIOROS
  // =======================================================

  deterioroCapital: number | null;

  deterioroIntereses: number | null;

  deterioroOtros: number | null;

  deterioroTotal: number | null;


  // =======================================================
  // APORTES
  // =======================================================

  saldoAportesFechaCorte: number | null;

  porcentajeAportesCredito: number | null;

  valorAportesCredito: number | null;


  // =======================================================
  // GARANTÍAS
  // =======================================================

  cantidadBienesGarantia: number | null;

  valorGarantiasTotal: number | null;

  porcentajeGarantiasCredito: number | null;

  valorGarantiasCredito: number | null;


  // =======================================================
  // ESTADO ACTUAL DEL MAESTRO
  // =======================================================

  saldoActualMaestro: number;

  codigoEstadoCarteraActual: string | null;

  creditoActivoActual: boolean;
}

// =========================================================
// ORIGINACIÓN DE CARTERA
// Contexto del asociado
// =========================================================


// =========================================================
// CONTEXTO PRINCIPAL
// =========================================================

export interface OriginacionContexto {

  idDatosPersonal: number;

  idAgencia: number;

  informacionEconomica:
    OriginacionInformacionEconomica | null;

  resumenCartera:
    OriginacionResumenCartera | null;

  reciprocidad:
    OriginacionReciprocidad | null;

  depositos:
    OriginacionDeposito[];

  carteraActual:
    OriginacionCartera[];

  vectorResumen:
    OriginacionVectorResumen[];

  codeudasActuales:
    OriginacionCodeuda[];
}


// =========================================================
// RESUMEN DE CARTERA
// =========================================================

export interface OriginacionResumenCartera {

  cantidadCreditos: number | null;

  saldoCarteraActual: number | null;

  moraActual: number | null;

  moraMaximaHistorica: number | null;

  promedioMoraUltimos12Meses: number | null;

  cantidadCreditosReclasificados: number | null;

  tieneCreditosReclasificados: boolean | null;
}


// =========================================================
// RECIPROCIDAD
// =========================================================

export interface OriginacionReciprocidad {

  saldoAportes: number | null;

  reciprocidadInicial: number | null;

  cupoReciprocidad: number | null;

  saldoCarteraActual: number | null;

  diferenciaReciprocidad: number | null;

  disponibleReciprocidad: number | null;

  excesoReciprocidad: number | null;
}


// =========================================================
// INFORMACIÓN ECONÓMICA
// =========================================================

export interface OriginacionInformacionEconomica {

  idFinanciero: number;

  idDatosPersonal: number;

  nombreActividadEconomica: string | null;

  nombreSectorEconomico: string | null;

  ocupacion: string | null;

  empresa: string | null;


  // Ingresos

  valorSalario: number | null;

  valorPension: number | null;

  ingresosArriendo: number | null;

  ingresosComisiones: number | null;

  otrosIngresos: number | null;

  comentarioOtrosIngresos: string | null;

  totalIngresos: number | null;


  // Egresos

  egresosFamiliares: number | null;

  egresosArriendo: number | null;

  egresosCredito: number | null;

  otrosEgresos: number | null;

  comentarioOtrosEgresos: string | null;

  totalEgresos: number | null;


  // Patrimonio

  totalActivos: number | null;

  totalPasivos: number | null;

  patrimonio: number | null;


  // Información adicional

  origenFondos: string | null;

  relacionFinanciera: string | null;

  deudaRelacionFinanciera: number | null;


  // Control

  fechaCreacion: string | null;

  fechaEdicion: string | null;
}


// =========================================================
// DEPÓSITOS
// =========================================================

export interface OriginacionDeposito {

  idCuentaAhorro: number;

  idAgencia: number;

  idDatosPersonal: number;

  idFormaAhorro: number;

  codigoForma: string | null;

  nombreForma: string | null;

  codigoCuenta: string | null;

  fechaApertura: string | null;

  fechaUltimoMovimiento: string | null;

  entradasUltimoAno: number | null;

  salidasUltimoAno: number | null;

  saldo: number | null;

  codigoEstado: string | null;

  nombreEstado: string | null;

  activa: boolean | null;
}


// =========================================================
// CARTERA
// =========================================================

export interface OriginacionCartera {

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string | null;

  fechaDesembolso: string | null;

  saldoActual: number | null;

  diasMora: number | null;

  codigoEstadoCartera: string | null;

  nombreEstadoCartera: string | null;

  vigente: boolean | null;
}


// =========================================================
// CODEUDAS
// =========================================================

export interface OriginacionCodeuda {

  idObligacionFiador: number;

  idObligacionJuridica: number;

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string | null;

  idDeudorPrincipal: number;

  documentoDeudorPrincipal: string | null;

  deudorPrincipal: string | null;

  fechaDesembolso: string | null;

  saldoActual: number | null;

  diasMora: number | null;

  codigoEstadoCartera: string | null;

  nombreEstadoCartera: string | null;

  vigente: boolean | null;
}


// =========================================================
// VECTOR - RESUMEN
// =========================================================

export interface OriginacionVectorResumen {

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string | null;

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string | null;

  nombreCompleto: string | null;


  // Período

  primerCorte: string | null;

  ultimoCorte: string | null;

  cantidadCortesObservados: number | null;


  // Mora

  moraUltimoCorte: number | null;

  moraMaxima: number | null;

  cantidadCortesAlDia: number | null;

  cantidadCortesConMora: number | null;

  pbbMora: number | null;


  // Crédito

  fechaDesembolso: string | null;

  valorInicialCredito: number | null;

  valorDesembolsado: number | null;


  // Saldos

  saldoPrimerCorte: number | null;

  saldoUltimoCorte: number | null;

  saldoActualMaestro: number | null;


  // Estado

  codigoEstadoCartera: string | null;

  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;

  descripcionEstadoJuridico: string | null;


  // Edades

  edadRiesgoInicialResultado: string | null;

  edadDeMoraResultado: string | null;

  edadDeRiesgoResultado: string | null;

  edadDePeResultado: string | null;

  edadDeHomologacionResultado: string | null;

  edadContableResultado: string | null;


  // Indicadores

  tuvoMoraPeriodo: boolean | null;

  estaEnMoraUltimoCorte: boolean | null;

  tuvoMoraMayor90: boolean | null;

  tieneHistoria: boolean | null;
}


// =========================================================
// VECTOR - DETALLE
// =========================================================

export interface OriginacionVectorDetalle {

  posicionVector: number;

  tipoPosicion: string | null;

  periodoVector: string | null;

  fechaReferencia: string | null;


  // Crédito

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string | null;


  // Mora

  diasMora: number | null;

  rangoMora: string | null;

  tieneMora: boolean | null;


  // Edades

  edadRiesgoInicialResultado: string | null;

  edadDeMoraResultado: string | null;

  edadDeRiesgoResultado: string | null;

  edadDePeResultado: string | null;

  edadDeHomologacionResultado: string | null;

  edadContableResultado: string | null;


  // Saldo

  saldoCreditoFechaCorte: number | null;

  saldoActualMaestro: number | null;


  // Estado

  codigoEstadoCartera: string | null;

  descripcionEstadoCartera: string | null;
}

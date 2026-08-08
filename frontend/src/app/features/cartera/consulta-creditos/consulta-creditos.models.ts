// =========================================================
// CONSULTA DE CRÉDITOS
// Modelos del frontend alineados con los DTO del backend
// =========================================================

// =========================================================
// LISTADO RESUMIDO DE CRÉDITOS
// =========================================================

export interface ConsultaCreditoResumen {

  // Identificación
  idCarteraCredito: number | null;
  pagareCartera: string | null;

  // Línea
  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  descripcionLineaCredito: string | null;

  // Estado
  codigoEstadoCartera: string | null;
  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;
  descripcionEstadoJuridico: string | null;

  // Fechas
  fechaDesembolso: string | null;
  fechaFinal: string | null;

  // Valores
  valorDesembolsado: number | null;
  saldoActual: number | null;

  // Riesgo
  edadDeRiesgo: string | null;
  descripcionEdadDeRiesgo: string | null;

  edadDeMora: string | null;
  descripcionEdadDeMora: string | null;

  // Indicadores
  creditoSaldado: boolean | null;
  creditoEnMora: boolean | null;
  riesgoAlto: boolean | null;
  requiereRevision: boolean | null;
}


// =========================================================
// DETALLE FUNCIONAL DEL CRÉDITO
// =========================================================

export interface ConsultaCreditoDetalle {

  // Identificación
  idCarteraCredito: number | null;
  pagareCartera: string | null;
  idDatosPersonal: number | null;
  idCuentaAportes: number | null;

  tipoComprobante: string | null;
  numeroComprobante: string | null;

  // Agencia
  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;

  // Línea
  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  // Clasificación
  codigoClasificacionCredito: string | null;
  descripcionClasificacionCredito: string | null;

  // Garantía
  codigoGarantiaCredito: string | null;
  descripcionGarantiaCredito: string | null;
  tipoGarantia: string | null;

  // Destino económico
  codigoDestinoEconomico: string | null;
  descripcionDestinoEconomico: string | null;

  // Modalidad de interés
  periodoCodigoInteres: string | null;
  tipoModalidadInteres: string | null;
  descripcionModalidadInteres: string | null;
  periodoInteresMeses: number | null;

  // Amortización
  amortizacionCapital: number | null;

  codigoTipoCuota: string | null;
  descripcionTipoCuota: string | null;

  plazo: number | null;
  mesesGraciaCapital: number | null;
  mesesGraciaInteres: number | null;

  codigoFormaPago: string | null;
  descripcionFormaPago: string | null;

  // Libranza y aprobación
  idEmpresaLibranza: number | null;

  idEnteAprobacion: number | null;
  nombreEnteAprobacion: string | null;

  // Estado de cartera
  codigoEstadoCartera: string | null;
  descripcionEstadoCartera: string | null;
  operativoEstadoCartera: string | null;

  // Estado jurídico
  codigoEstadoJuridico: string | null;
  descripcionEstadoJuridico: string | null;
  fechaEstadoJuridico: string | null;

  // Valores principales
  valorInicialCredito: number | null;
  valorDesembolsado: number | null;
  valorBaseCalculoCuota: number | null;

  valorPrimeraCuota: number | null;
  valorCuota: number | null;

  // Saldos
  saldoActual: number | null;
  abonosPendientes: number | null;
  saldoNetoPendiente: number | null;

  // Ejecución
  porcentajeSaldoSobreDesembolso: number | null;
  porcentajeCapitalAmortizado: number | null;
  capitalAmortizadoEstimado: number | null;

  alturaCuota: number | null;
  cuotasPendientesEstimadas: number | null;

  porcentajePlazoTranscurrido: number | null;

  // Tasas
  tasaNominalAnual: number | null;
  tasaEfectivaAnual: number | null;

  // Fechas principales
  fechaInclusionSistema: string | null;
  fechaContable: string | null;
  fechaDesembolso: string | null;

  fechaPrimeraCuota: string | null;
  fechaPrimeraCuotaCapital: string | null;
  fechaPrimeraCuotaInteres: string | null;

  fechaFinal: string | null;

  // Indicadores de fechas
  diasDesdeDesembolso: number | null;
  diasParaVencimiento: number | null;
  diasVencidoPorFechaFinal: number | null;

  vencidoPorFechaFinal: boolean | null;

  // Últimas fechas por concepto
  ultimaFechaCapital: string | null;
  ultimaFechaInteres: string | null;
  ultimaFechaMora: string | null;
  ultimaFechaSeguro: string | null;
  ultimaFechaFondo: string | null;

  // Próximas fechas por concepto
  proximaFechaCapital: string | null;
  proximaFechaInteres: string | null;
  proximaFechaSeguro: string | null;
  proximaFechaFondo: string | null;

  interesesPagadosHasta: string | null;
  interesesMoraHasta: string | null;

  // Días para próximos vencimientos
  diasParaProximoCapital: number | null;
  diasParaProximoInteres: number | null;
  diasParaProximoSeguro: number | null;
  diasParaProximoFondo: number | null;

  // Evaluación registrada en el crédito
  creditoEvaluado: boolean | null;
  fechaEvaluacion: string | null;
  diasDesdeUltimaEvaluacion: number | null;
  comentarioEvaluacion: string | null;

  // Riesgo
  edadDeRiesgo: string | null;
  descripcionEdadDeRiesgo: string | null;

  edadRiesgoInicial: string | null;
  descripcionEdadRiesgoInicial: string | null;

  edadDeMora: string | null;
  descripcionEdadDeMora: string | null;

  edadDePe: string | null;
  descripcionEdadDePe: string | null;

  edadDeHomologacion: string | null;
  descripcionEdadDeHomologacion: string | null;

  edadContable: string | null;
  descripcionEdadContable: string | null;

  ordenEdadRiesgo: number | null;
  ordenEdadMora: number | null;
  ordenEdadContable: number | null;

  // Indicadores de riesgo
  creditoEnMora: boolean | null;
  riesgoAlto: boolean | null;
  moraAlta: boolean | null;
  deterioroContableAlto: boolean | null;

  riesgoDeterioradoDesdeInicial: boolean | null;
  riesgoMejoradoDesdeInicial: boolean | null;

  // Modificaciones
  codigoModificacionCredito: string | null;
  descripcionModificacionCredito: string | null;

  numeroNovaciones: number | null;

  creditoNovado: boolean | null;
  creditoReestructurado: boolean | null;

  fechaReestructuracion: string | null;

  edadReestructurado: string | null;
  descripcionEdadReestructurado: string | null;

  comentarioRestructuracion: string | null;
  diasDesdeReestructuracion: number | null;

  // Centrales de riesgo
  ultimaFechaCifin: string | null;
  ultimaFechaDatacredito: string | null;
  ultimaFechaOtra: string | null;

  diasDesdeConsultaCifin: number | null;
  diasDesdeConsultaDatacredito: number | null;
  diasDesdeOtraConsulta: number | null;

  // Información complementaria
  establecimiento: string | null;
  comentarioGeneral: string | null;

  // Estado financiero y operativo
  tieneSaldo: boolean | null;
  creditoSaldado: boolean | null;
  tieneAbonosPendientes: boolean | null;
  enCobroJuridico: boolean | null;

  // Alertas
  requiereRevision: boolean | null;

  nivelAlertaCredito: string | null;
  ordenAlertaCredito: number | null;
  motivoAlertaCredito: string | null;

  // Auditoría
  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}


// =========================================================
// EXTRACTO DEL CRÉDITO
// =========================================================

export interface ConsultaCreditoExtracto {

  // Identificación
  idExtractoCartera: number | null;
  idCarteraCredito: number | null;
  pagareCartera: string | null;
  idDatosPersonal: number | null;

  // Agencia
  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;

  // Línea
  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  // Estado
  codigoEstadoCartera: string | null;
  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;
  descripcionEstadoJuridico: string | null;

  // Clasificación
  codigoClasificacionCredito: string | null;
  descripcionClasificacionCredito: string | null;

  // Garantía
  codigoGarantiaCredito: string | null;
  descripcionGarantiaCredito: string | null;
  tipoGarantia: string | null;

  // Riesgo y mora
  edadDeRiesgo: string | null;
  descripcionEdadDeRiesgo: string | null;

  edadDeMora: string | null;
  descripcionEdadDeMora: string | null;

  // Valores generales
  valorInicialCredito: number | null;
  valorDesembolsado: number | null;
  valorCuota: number | null;
  saldoActualCredito: number | null;

  // Fecha y comprobante
  fechaPago: string | null;
  hora: string | null;
  fechaContable: string | null;

  diasDesdePago: number | null;

  tipoComprobante: string | null;
  numeroComprobante: string | null;
  comprobanteCompleto: string | null;

  // Aplicación por concepto
  valorCapital: number | null;

  valorInteresCausado: number | null;
  valorInteresIngreso: number | null;
  valorInteresAnticipado: number | null;
  valorInteresMora: number | null;

  valorSeguro: number | null;
  valorAportes: number | null;
  valorPapeleria: number | null;
  valorFondoGarantia: number | null;
  otrosValores: number | null;

  // Totales
  totalInteresesRegistrados: number | null;
  totalOtrosConceptos: number | null;
  totalComponentesRegistrados: number | null;

  // Medios de pago
  pagoEfectivo: number | null;
  pagoCheques: number | null;
  pagoNotas: number | null;

  idCuentaAhorro: number | null;
  codigoCuentaAhorro: string | null;
  nombreFormaAhorro: string | null;
  saldoActualCuentaAhorro: number | null;

  pagoConsignacion: number | null;
  pagoPse: number | null;
  otroPago: number | null;

  totalMediosPago: number | null;
  diferenciaMediosPagoComponentes: number | null;

  // Control de medios
  movimientoCuadrado: boolean | null;

  usoEfectivo: boolean | null;
  usoCheques: boolean | null;
  usoNotas: boolean | null;
  usoCuentaAhorro: boolean | null;
  usoConsignacion: boolean | null;
  usoPse: boolean | null;
  usoOtroPago: boolean | null;

  cantidadMediosPagoUtilizados: number | null;
  medioPagoPrincipal: string | null;

  // Observaciones
  observacionPago: string | null;
  tieneObservacion: boolean | null;

  // Mora
  diasCapitalIngreso: number | null;
  diasInteresIngreso: number | null;
  diasMora: number | null;

  pagoConMora: boolean | null;

  rangoDiasMora: string | null;
  ordenRangoMora: number | null;

  // Ejecución
  alturaCuota: number | null;
  abonosPendientes: number | null;

  // Fechas por concepto
  ultimaFechaCapital: string | null;
  ultimaFechaInteres: string | null;
  ultimaFechaSeguro: string | null;
  ultimaFechaMora: string | null;

  proximaFechaCapital: string | null;
  proximaFechaInteres: string | null;
  proximaFechaSeguro: string | null;

  interesesPagadosHasta: string | null;

  // Origen
  tarjeta: string | null;
  modulo: string | null;
  movimientoConTarjeta: boolean | null;

  // Estado
  estado: string | null;
  movimientoActivo: boolean | null;

  // Conceptos aplicados
  aplicoCapital: boolean | null;
  aplicoIntereses: boolean | null;
  aplicoInteresMora: boolean | null;
  aplicoSeguro: boolean | null;
  aplicoAportes: boolean | null;
  aplicoFondoGarantia: boolean | null;
  aplicoPapeleria: boolean | null;

  // Indicadores
  porcentajePagoAplicadoCapital: number | null;

  // Secuencia y acumulados
  numeroMovimientoCredito: number | null;
  cantidadMovimientosCredito: number | null;

  capitalAcumuladoHastaMovimiento: number | null;
  recaudoAcumuladoHastaMovimiento: number | null;

  fechaUltimoMovimientoCredito: string | null;
  perteneceUltimaFechaMovimiento: boolean | null;

  // Auditoría
  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}


// =========================================================
// SEGUROS DEL CRÉDITO
// =========================================================

export interface ConsultaCreditoSeguro {

  // Identificación
  idCreditoSeguro: number | null;
  idCarteraCredito: number | null;
  pagareCartera: string | null;
  idDatosPersonal: number | null;

  // Agencia
  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;

  // Línea
  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  // Estado
  codigoEstadoCartera: string | null;
  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;
  descripcionEstadoJuridico: string | null;

  // Clasificación y garantía
  codigoClasificacionCredito: string | null;
  descripcionClasificacionCredito: string | null;

  codigoGarantiaCredito: string | null;
  descripcionGarantiaCredito: string | null;

  // Configuración
  porcentajeSeguro: number | null;

  tipoCobroSeguro: string | null;
  descripcionTipoCobroSeguro: string | null;

  seguroActivo: boolean | null;
  descripcionEstadoSeguro: string | null;

  // Valores estimados
  valorSeguroSobreDesembolsoEstimado: number | null;
  valorSeguroSobreSaldoEstimado: number | null;
  valorSeguroSobreSaldoNetoEstimado: number | null;
  valorSeguroSobreCuotaEstimado: number | null;

  // Validaciones
  porcentajeSeguroValido: boolean | null;
  porcentajeSeguroNegativo: boolean | null;
  porcentajeSeguroSuperaCien: boolean | null;

  tieneTipoCobroSeguro: boolean | null;
  configuracionSeguroCompleta: boolean | null;

  // Resumen
  cantidadConfiguracionesSeguro: number | null;
  cantidadSegurosActivos: number | null;
  cantidadSegurosInactivos: number | null;
  cantidadSegurosActivosValidos: number | null;

  porcentajeTotalSeguroActivo: number | null;

  tieneSeguroActivo: boolean | null;
  tieneSeguroActivoValido: boolean | null;
  tienePorcentajeInconsistente: boolean | null;
  tieneMultiplesSegurosActivos: boolean | null;
  tieneHistorialConfiguracionesSeguro: boolean | null;

  // Fechas
  ultimaFechaSeguro: string | null;
  proximaFechaSeguro: string | null;

  diasParaProximoSeguro: number | null;

  fechaSeguroVencida: boolean | null;
  diasSeguroVencido: number | null;

  estadoVencimientoSeguro: string | null;

  // Alertas
  nivelAlertaSeguro: string | null;
  motivoAlertaSeguro: string | null;
  ordenAlertaSeguro: number | null;

  // Historial
  numeroConfiguracionSeguro: number | null;
  cantidadRegistrosSeguroCredito: number | null;
  configuracionSeguroPrincipal: boolean | null;

  // Riesgo
  edadDeRiesgo: string | null;
  descripcionEdadDeRiesgo: string | null;

  edadDeMora: string | null;
  descripcionEdadDeMora: string | null;

  // Estado financiero
  saldoActual: number | null;
  saldoNetoPendiente: number | null;

  creditoSaldado: boolean | null;
  requiereRevision: boolean | null;

  // Auditoría
  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}


// =========================================================
// ALIVIOS DEL CRÉDITO
// =========================================================

export interface ConsultaCreditoAlivio {

  idCreditoAlivio: number | null;
  idCarteraCredito: number | null;
  pagareCartera: string | null;
  idDatosPersonal: number | null;

  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;

  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  codigoEstadoCartera: string | null;
  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;
  descripcionEstadoJuridico: string | null;

  idTipoAlivio: number | null;
  codigoTipoAlivio: string | null;
  descripcionTipoAlivio: string | null;

  numeroActa: string | null;
  numeroResolucion: string | null;

  fechaAlivio: string | null;
  fechaAplicacion: string | null;

  periodoInicial: string | null;
  periodoFinal: string | null;

  cantidadPeriodos: number | null;

  valorCapital: number | null;
  valorInteresCorriente: number | null;
  valorInteresMora: number | null;
  valorSeguro: number | null;
  valorFondoGarantia: number | null;
  valorOtrosConceptos: number | null;
  valorTotalAlivio: number | null;

  valorCapitalAplicado: number | null;
  valorInteresAplicado: number | null;
  valorMoraAplicada: number | null;
  valorSeguroAplicado: number | null;
  valorOtrosAplicados: number | null;
  valorTotalAplicado: number | null;

  saldoCapital: number | null;
  saldoInteres: number | null;
  saldoMora: number | null;
  saldoSeguro: number | null;
  saldoOtrosConceptos: number | null;
  saldoPendiente: number | null;

  alivioActivo: boolean | null;
  alivioFinalizado: boolean | null;
  alivioAnulado: boolean | null;
  requiereRevision: boolean | null;

  cuotasBeneficiadas: number | null;
  cuotasPendientes: number | null;

  porcentajeAplicado: number | null;
  porcentajePendiente: number | null;

  observacion: string | null;
  comentario: string | null;

  nivelAlertaAlivio: string | null;
  ordenAlertaAlivio: number | null;
  motivoAlertaAlivio: string | null;

  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}


// =========================================================
// INTERESES CAUSADOS
// =========================================================

export interface ConsultaCreditoInteres {

  idInteresCausado: number | null;
  idCarteraCredito: number | null;
  pagareCartera: string | null;
  idDatosPersonal: number | null;

  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;

  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  codigoEstadoCartera: string | null;
  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;
  descripcionEstadoJuridico: string | null;

  fechaProceso: string | null;
  periodoInicial: string | null;
  periodoFinal: string | null;

  diasLiquidados: number | null;

  saldoCapital: number | null;
  saldoNetoPendiente: number | null;
  saldoBaseLiquidacion: number | null;

  tasaNominal: number | null;
  tasaEfectiva: number | null;
  tasaMora: number | null;

  interesCorrienteCausado: number | null;
  interesMoraCausado: number | null;
  seguroCausado: number | null;
  fondoGarantiaCausado: number | null;
  otrosConceptosCausados: number | null;
  totalCausado: number | null;

  interesCorrientePagado: number | null;
  interesMoraPagado: number | null;
  seguroPagado: number | null;
  fondoGarantiaPagado: number | null;
  otrosConceptosPagados: number | null;
  totalPagado: number | null;

  saldoInteresCorriente: number | null;
  saldoInteresMora: number | null;
  saldoSeguro: number | null;
  saldoFondoGarantia: number | null;
  saldoOtrosConceptos: number | null;
  saldoPendiente: number | null;

  edadDeRiesgo: string | null;
  descripcionEdadDeRiesgo: string | null;

  edadDeMora: string | null;
  descripcionEdadDeMora: string | null;

  procesoActivo: boolean | null;
  procesoAplicado: boolean | null;
  requiereRevision: boolean | null;

  porcentajeCobrado: number | null;
  porcentajePendiente: number | null;
  numeroProceso: number | null;

  observacion: string | null;
  comentario: string | null;

  nivelAlerta: string | null;
  ordenAlerta: number | null;
  motivoAlerta: string | null;

  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}


// =========================================================
// EVALUACIONES DE CARTERA
// =========================================================

export interface ConsultaCreditoEvaluacion {

  idEvaluacionCartera: number | null;
  idCarteraCredito: number | null;

  pagareCartera: string | null;
  idDatosPersonal: number | null;

  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;

  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  codigoEstadoCartera: string | null;
  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;
  descripcionEstadoJuridico: string | null;

  numeroEvaluacion: number | null;

  fechaEvaluacion: string | null;
  fechaContable: string | null;

  ultimaEvaluacionCredito: boolean | null;

  edadRiesgoAnterior: string | null;
  descripcionEdadRiesgoAnterior: string | null;

  edadRiesgoInicial: string | null;
  descripcionEdadRiesgoInicial: string | null;

  edadMora: string | null;
  descripcionEdadMora: string | null;

  edadRiesgoEvaluada: string | null;
  descripcionEdadRiesgoEvaluada: string | null;

  edadRiesgoNueva: string | null;
  descripcionEdadRiesgoNueva: string | null;

  resultadoEvaluacion: string | null;
  accionEvaluacion: string | null;
  variacionRiesgo: string | null;

  riesgoMejora: boolean | null;
  riesgoIgual: boolean | null;
  riesgoDeteriora: boolean | null;

  ordenEdadRiesgoAnterior: number | null;
  ordenEdadRiesgoNueva: number | null;
  diferenciaNivelRiesgo: number | null;

  saldoCapital: number | null;
  saldoInteresCorriente: number | null;
  saldoInteresMora: number | null;
  saldoSeguro: number | null;
  saldoOtrosConceptos: number | null;
  saldoTotal: number | null;

  evaluacionAplicada: boolean | null;
  requiereRevision: boolean | null;

  comentarioEvaluacion: string | null;
  observacion: string | null;

  nivelAlertaEvaluacion: string | null;
  ordenAlertaEvaluacion: number | null;
  motivoAlertaEvaluacion: string | null;

  idUsuarioEvaluacion: number | null;
  usuarioEvaluacion: string | null;

  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}


// =========================================================
// CONSULTA INTEGRAL
// =========================================================

export interface ConsultaCreditoIntegral {

  credito: ConsultaCreditoDetalle | null;

  extracto: ConsultaCreditoExtracto[];

  seguros: ConsultaCreditoSeguro[];

  alivios: ConsultaCreditoAlivio[];

  interesesCausados: ConsultaCreditoInteres[];

  evaluaciones: ConsultaCreditoEvaluacion[];

  ultimaEvaluacion: ConsultaCreditoEvaluacion | null;

  prorrogas: ConsultaCreditoProrroga[];
}

// =========================================================
// ASOCIADO ENCONTRADO PARA PROCESOS DE CARTERA
// =========================================================

export interface CarteraAsociadoBusqueda {

  idDatosPersonal: number;

  tipoDocumento: string | null;

  nombreTipoDocumento: string | null;

  documento: string;

  nombres: string | null;

  primerApellido: string | null;

  segundoApellido: string | null;

  nombreCompleto: string;

  cantidadCreditos: number;

}

export interface ConsultaCreditoProrroga {

  idCreditoProrroga: number | null;

  idCarteraCredito: number | null;

  numeroProrroga: number | null;

  fechaProrroga: string | null;

  fechaCapitalAnterior: string | null;

  fechaCapitalNueva: string | null;

  mesesProrroga: number | null;

  valorTotalLiquidado: number | null;

  valorTotalPagado: number | null;

  estadoProrroga: string | null;

  observacion: string | null;

}

// =========================================================
// ESTADO INICIAL
// =========================================================

export function crearConsultaCreditoIntegralVacia():
ConsultaCreditoIntegral {

  return {
    credito: null,
    extracto: [],
    seguros: [],
    alivios: [],
    interesesCausados: [],
    evaluaciones: [],
    prorrogas: [],
    ultimaEvaluacion: null

  };

}

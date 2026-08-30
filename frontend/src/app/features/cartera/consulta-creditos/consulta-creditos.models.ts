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

export interface ConsultaCreditoSeguroMovimiento {

  // Identificación
  idCreditoSeguroDetalle: number | null;
  idCreditoSeguro: number | null;
  idCarteraCredito: number | null;

  // Fecha
  fechaMovimiento: string | null;

  // Base de liquidación
  saldoBase: number | null;
  interesesCausadosBase: number | null;
  otrosBase: number | null;
  baseCalculo: number | null;

  // Porcentajes
  porcentajeBaseSeguro: number | null;
  porcentajeExtraprima: number | null;
  porcentajeAplicar: number | null;

  // Condiciones aplicadas
  sobreSaldoActual: boolean | null;
  incluyeInteresesCausados: boolean | null;

  // Movimiento
  valorDebito: number | null;
  valorCredito: number | null;
  valorMovimiento: number | null;

  naturalezaMovimiento: string | null;

  // Saldo acumulado
  saldoSeguroAcumuladoCredito: number | null;
  saldoSeguroAcumuladoConfiguracion: number | null;

  // Comprobante
  tipoComprobante: string | null;
  numeroComprobante: string | null;
  comprobanteCompleto: string | null;

  // Estado
  estado: string | null;
  movimientoActivo: boolean | null;

  // Secuencia
  numeroMovimientoCredito: number | null;
  numeroMovimientoSeguro: number | null;

  cantidadMovimientosCredito: number | null;
  cantidadMovimientosSeguro: number | null;

  // Observación
  observacionSeguro: string | null;

  // Auditoría
  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;

  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}


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

  // Historial de configuraciones
  numeroConfiguracionSeguro: number | null;
  cantidadRegistrosSeguroCredito: number | null;
  configuracionSeguroPrincipal: boolean | null;

  // Movimientos
  movimientos: ConsultaCreditoSeguroMovimiento[];

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

  // =========================================================
  // Identificación
  // =========================================================

  idInteresCausado: number | null;
  idCarteraCredito: number | null;

  pagareCartera: string | null;
  idDatosPersonal: number | null;


  // =========================================================
  // Agencia
  // =========================================================

  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;


  // =========================================================
  // Crédito
  // =========================================================

  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  codigoEstadoCartera: string | null;
  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;
  descripcionEstadoJuridico: string | null;


  // =========================================================
  // Movimiento
  // =========================================================

  fechaMovimiento: string | null;

  fechaInicialPeriodo: string | null;
  fechaFinalPeriodo: string | null;

  diasCausados: number | null;


  // =========================================================
  // Base de liquidación
  // =========================================================

  saldoBase: number | null;

  interesBruto: number | null;

  porcentajeAplicacion: number | null;


  // =========================================================
  // Tasa
  // =========================================================

  tasaInteres: number | null;


  // =========================================================
  // Débito / Crédito
  // =========================================================

  valorDebito: number | null;
  valorCredito: number | null;

  valorMovimiento: number | null;

  naturalezaMovimiento: string | null;


  // =========================================================
  // Saldo acumulado
  // =========================================================

  saldoInteresesAcumulado: number | null;


  // =========================================================
  // Comprobante
  // =========================================================

  tipoComprobante: string | null;
  numeroComprobante: string | null;
  comprobanteCompleto: string | null;


  // =========================================================
  // Observación
  // =========================================================

  observacionInteres: string | null;


  // =========================================================
  // Control del período
  // =========================================================

  diasCalendarioPeriodo: number | null;
  diasCausadosCoincidenPeriodo: boolean | null;


  // =========================================================
  // Secuencia
  // =========================================================

  numeroMovimientoCredito: number | null;
  cantidadMovimientosCredito: number | null;


  // =========================================================
  // Resumen del crédito
  // =========================================================

  cantidadMovimientos: number | null;

  cantidadDebitos: number | null;
  cantidadCreditos: number | null;

  totalDebitos: number | null;
  totalCreditos: number | null;

  saldoIntereses: number | null;

  primeraFechaMovimiento: string | null;
  ultimaFechaMovimiento: string | null;


  // =========================================================
  // Riesgo
  // =========================================================

  edadDeRiesgo: string | null;
  descripcionEdadDeRiesgo: string | null;

  edadDeMora: string | null;
  descripcionEdadDeMora: string | null;

  edadContable: string | null;
  descripcionEdadContable: string | null;


  // =========================================================
  // Estado financiero
  // =========================================================

  saldoActual: number | null;
  saldoNetoPendiente: number | null;

  creditoEnMora: boolean | null;
  riesgoAlto: boolean | null;
  requiereRevision: boolean | null;


  // =========================================================
  // Alertas
  // =========================================================

  nivelAlertaCredito: string | null;
  motivoAlertaCredito: string | null;


  // =========================================================
  // Compatibilidad temporal
  // =========================================================

  fechaProceso: string | null;

  periodoInicial: string | null;
  periodoFinal: string | null;

  diasLiquidados: number | null;

  saldoCapital: number | null;
  saldoBaseLiquidacion: number | null;

  tasaNominal: number | null;

  // Compatibilidad con el extracto actual
  saldoInteresCorriente: number | null;
  saldoInteresMora: number | null;

  saldoSeguro: number | null;
  saldoFondoGarantia: number | null;
  saldoOtrosConceptos: number | null;

  saldoPendiente: number | null;

  observacion: string | null;


  // =========================================================
  // Auditoría
  // =========================================================

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

  resultadosMensuales: ConsultaCreditoResultadoMensual[];
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

  // =========================================================
  // Identificación
  // =========================================================

  idCreditoProrroga: number | null;
  idCarteraCredito: number | null;
  numeroProrroga: number | null;

  fechaProrroga: string | null;

  diasProrroga: number | null;
  mesesProrroga: number | null;

  // =========================================================
  // Situación anterior
  // =========================================================

  proximaFechaCapitalAnterior: string | null;
  proximaFechaInteresAnterior: string | null;

  fechaVencimientoAnterior: string | null;
  plazoAnterior: number | null;

  // =========================================================
  // Situación nueva
  // =========================================================

  proximaFechaCapitalNueva: string | null;
  proximaFechaInteresNueva: string | null;

  fechaVencimientoNueva: string | null;
  plazoNuevo: number | null;

  // =========================================================
  // Valores liquidados
  // =========================================================

  valorInteresCorriente: number | null;
  valorInteresMora: number | null;
  valorSeguro: number | null;
  valorAportes: number | null;
  valorFondoGarantia: number | null;
  valorOtrosConceptos: number | null;

  valorTotalLiquidado: number | null;

  // =========================================================
  // Valores pagados
  // =========================================================

  valorInteresCorrientePagado: number | null;
  valorInteresMoraPagado: number | null;
  valorSeguroPagado: number | null;
  valorAportesPagado: number | null;
  valorFondoGarantiaPagado: number | null;
  valorOtrosConceptosPagado: number | null;

  valorTotalPagado: number | null;

  // =========================================================
  // Comprobante
  // =========================================================

  tipoComprobante: string | null;
  numeroComprobante: string | null;
  fechaComprobante: string | null;

  idAgencia: number | null;

  // =========================================================
  // Estado
  // =========================================================

  estadoProrroga: string | null;

  fechaAplicacion: string | null;
  fechaAnulacion: string | null;

  motivoAnulacion: string | null;
  observacion: string | null;
}

// =========================================================
// RESULTADOS MENSUALES DE CARTERA
// =========================================================

export interface ConsultaCreditoResultadoMensual {

  // =========================================================
  // Cierre
  // =========================================================

  idCierreCartera: number | null;

  fechaCorte: string | null;
  estadoCierre: string | null;

  anioCorte: number | null;
  mesCorte: number | null;
  periodoCorte: string | null;


  // =========================================================
  // Crédito
  // =========================================================

  idCierreCarteraCredito: number | null;
  idCarteraCredito: number | null;

  idAgencia: number | null;

  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;

  pagareCartera: string | null;


  // =========================================================
  // Estado del crédito en el corte
  // =========================================================

  codigoEstadoCartera: string | null;
  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;
  descripcionEstadoJuridico: string | null;

  codigoClasificacionCredito: string | null;
  descripcionClasificacionCredito: string | null;


  // =========================================================
  // Condiciones
  // =========================================================

  plazo: number | null;

  valorCuota: number | null;

  tasaNominalAnual: number | null;
  tasaEfectivaAnual: number | null;


  // =========================================================
  // Fechas
  // =========================================================

  fechaFinal: string | null;

  ultimaFechaCapital: string | null;
  ultimaFechaInteres: string | null;

  proximaFechaCapital: string | null;
  proximaFechaInteres: string | null;


  // =========================================================
  // Resultado mensual
  // =========================================================

  idCierreCarteraResultado: number | null;

  codigoMetodoCalculo: string | null;

  esUnaSolaCuota: boolean | null;
  esReestructurado: boolean | null;

  diasMora: number | null;
  diasDiferencia: number | null;


  // =========================================================
  // Edades resultantes
  // =========================================================

  edadRiesgoInicialResultado: string | null;
  edadDeMoraResultado: string | null;
  edadDeRiesgoResultado: string | null;
  edadDePeResultado: string | null;
  edadDeHomologacionResultado: string | null;
  edadContableResultado: string | null;

  edadReestructuracionInicialResultado: string | null;
  edadReestructuradoResultado: string | null;


  // =========================================================
  // Saldos
  // =========================================================

  saldoActualFotografia: number | null;
  saldoActualResultado: number | null;
  saldoCreditoFechaCorte: number | null;


  // =========================================================
  // Concentración del asociado
  // =========================================================

  cantidadCreditosAsociado: number | null;
  saldoTotalCreditosAsociado: number | null;


  // =========================================================
  // Aportes
  // =========================================================

  saldoAportesFechaCorte: number | null;
  porcentajeAportesCredito: number | null;
  valorAportesCredito: number | null;


  // =========================================================
  // Garantías
  // =========================================================

  cantidadBienesGarantia: number | null;
  valorGarantiasTotal: number | null;
  porcentajeGarantiasCredito: number | null;
  valorGarantiasCredito: number | null;


  // =========================================================
  // Exposición
  // =========================================================

  vea: number | null;
  exposicionTotalCalculada: number | null;


  // =========================================================
  // Intereses
  // =========================================================

  saldoInteresesCausados: number | null;
  valorInteresesCausadosMes: number | null;

  saldoInteresesContingentes: number | null;
  valorInteresesContingentesMes: number | null;


  // =========================================================
  // Otros conceptos
  // =========================================================

  valorCostasJudiciales: number | null;

  saldoSeguros: number | null;
  valorSegurosMes: number | null;

  saldoAlivios: number | null;
  valorAliviosMes: number | null;

  valorFondosGarantias: number | null;
  valorOtrosConceptos: number | null;


  // =========================================================
  // Pérdida esperada
  // =========================================================

  pi: number | null;
  pdi: number | null;
  perdidaEsperada: number | null;


  // =========================================================
  // Deterioros
  // =========================================================

  deterioroCapital: number | null;
  deterioroIntereses: number | null;
  deterioroOtros: number | null;

  deterioroTotal: number | null;

  porcentajeDeterioroCapital: number | null;


  // =========================================================
  // Cálculo
  // =========================================================

  fechaCalculo: string | null;
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
    resultadosMensuales: [],
    ultimaEvaluacion: null
  };

}

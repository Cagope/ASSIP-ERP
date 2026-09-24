// =========================================================
// DETALLE DE FORMALIZACIÓN
//
// Backend:
// SolicitudFormalizacionDetalleDTO
// =========================================================

export interface SolicitudFormalizacionDetalle {

  // =======================================================
  // IDENTIFICACIÓN DE LA SOLICITUD
  // =======================================================

  idSolicitudCredito: number;
  numeroSolicitud: string;

  idAgencia: number;
  nombreAgencia: string;

  idDatosPersonal: number;
  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;

  // =======================================================
  // ESTADO DEL PROCESO
  // =======================================================

  idSolicitudProceso: number;
  nombreProceso: string;

  idSolicitudResultado: number;
  nombreResultado: string;

  // =======================================================
  // CRÉDITO SOLICITADO
  // =======================================================

  idLineaCredito: number | null;
  nombreLineaCredito: string | null;

  valorSolicitado: number | null;
  plazoSolicitado: number | null;

  codigoFormaPago: string | null;
  periodoCodigoInteres: string | null;
  tipoModalidadInteres: string | null;

  amortizacionCapital: number | null;
  codigoTipoCuota: string | null;

  mesesGraciaCapital: number | null;
  mesesGraciaInteres: number | null;

  tasaColocacionAplicada: number | null;
  tasaEfectivaAnual: number | null;
  valorCuotaProyectada: number | null;

  // =======================================================
  // GARANTÍAS
  // =======================================================

  codigoGarantiaCredito: string | null;
  nombreGarantiaCredito: string | null;

  idFondoGarantia: number | null;
  nombreFondoGarantia: string | null;

  // =======================================================
  // DECISIÓN DEL ENTE APROBADOR
  // =======================================================

  idEnteAprobacion: number | null;

  fechaDecision: string | null;
  observacionesAprobacion: string | null;

  numeroActa: string | null;
  fechaActa: string | null;

  // =======================================================
  // CONDICIONES DEFINITIVAS DE FORMALIZACIÓN
  // =======================================================

  valorFormalizado: number | null;
  plazoFormalizado: number | null;

  codigoFormaPagoFormalizada: string | null;
  periodoCodigoInteresFormalizado: string | null;
  tipoModalidadInteresFormalizado: string | null;

  amortizacionCapitalFormalizada: number | null;
  codigoTipoCuotaFormalizada: string | null;

  mesesGraciaCapitalFormalizados: number | null;
  mesesGraciaInteresFormalizados: number | null;

  tasaNominalFormalizada: number | null;
  tasaEfectivaAnualFormalizada: number | null;
  valorCuotaFormalizada: number | null;

  // =======================================================
  // CONCEPTO FINAL DE FORMALIZACIÓN
  // =======================================================

  conceptoFormalizacion: string | null;

  // =======================================================
  // CONTROL DE FORMALIZACIÓN
  // =======================================================

  condicionesModificadas: boolean | null;

  fechaFinAprobacion: string | null;
  fechaFinFormalizacion: string | null;

  // =======================================================
  // PAGARÉ GENERADO
  // =======================================================

  idCarteraCredito: number | null;
  pagareCartera: string | null;
  codigoEstadoCartera: string | null;

}


// =========================================================
// GUARDAR / VALIDAR / SIMULAR CONDICIONES
//
// Backend:
// SolicitudFormalizacionGuardarRequestDTO
// =========================================================

export interface SolicitudFormalizacionGuardarRequest {

  valorFormalizado: number;
  plazoFormalizado: number;

  codigoFormaPagoFormalizada: string;
  periodoCodigoInteresFormalizado: string;
  tipoModalidadInteresFormalizado: string;

  amortizacionCapitalFormalizada: number;
  codigoTipoCuotaFormalizada: string;

  mesesGraciaCapitalFormalizados: number;
  mesesGraciaInteresFormalizados: number;

  tasaNominalFormalizada: number;

  conceptoFormalizacion: string | null;

}


// =========================================================
// RESULTADO DE SIMULACIÓN FINANCIERA
//
// Backend:
// SolicitudFormalizacionService.ResultadoSimulacionFinanciera
//
// La cuota se calcula sin guardar condiciones.
// =========================================================

export interface SolicitudFormalizacionSimulacion {

  tasaEfectivaAnual: number;
  valorCuota: number;

}


// =========================================================
// BANDEJA DE FORMALIZACIÓN
//
// Backend:
// SolicitudFormalizacionBandejaDTO
// =========================================================

export type EstadoFormalizacion =
  | 'PENDIENTE'
  | 'CONDICIONES_GUARDADAS'
  | 'PAGARE_GENERADO';

export interface SolicitudFormalizacionBandeja {

  // =======================================================
  // IDENTIFICACIÓN DE LA SOLICITUD
  // =======================================================

  idSolicitudCredito: number;
  numeroSolicitud: string;

  // =======================================================
  // AGENCIA
  // =======================================================

  idAgencia: number;
  nombreAgencia: string;

  // =======================================================
  // ASOCIADO
  // =======================================================

  idDatosPersonal: number;
  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;

  // =======================================================
  // LÍNEA DE CRÉDITO
  // =======================================================

  idLineaCredito: number | null;
  nombreLineaCredito: string | null;

  // =======================================================
  // CONDICIONES SOLICITADAS
  // =======================================================

  valorSolicitado: number | null;
  plazoSolicitado: number | null;

  // =======================================================
  // CONDICIONES DEFINITIVAS
  // =======================================================

  valorFormalizado: number | null;
  plazoFormalizado: number | null;

  // =======================================================
  // FECHAS DE CONTROL
  // =======================================================

  fechaFinAprobacion: string | null;
  fechaUltimaGestion: string | null;

  // =======================================================
  // CONTROL DE FORMALIZACIÓN
  // =======================================================

  condicionesModificadas: boolean | null;

  // =======================================================
  // CRÉDITO Y PAGARÉ
  // =======================================================

  idCarteraCredito: number | null;
  pagareCartera: string | null;

  // =======================================================
  // ESTADO DE LA BANDEJA
  // =======================================================

  estadoFormalizacion: EstadoFormalizacion;

}

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
  // CONTROL DE FORMALIZACIÓN
  // =======================================================

  condicionesModificadas: boolean | null;

  fechaFinAprobacion: string | null;
  fechaFinFormalizacion: string | null;

  // =======================================================
  // PAGARÉ GENERADO
  //
  // Se reciben después de constituir el crédito.
  // Antes de generar el pagaré pueden ser null.
  // =======================================================

  idCarteraCredito: number | null;
  pagareCartera: string | null;
  codigoEstadoCartera: string | null;

}


// =========================================================
// GUARDAR CONDICIONES DE FORMALIZACIÓN
//
// Backend:
// SolicitudFormalizacionGuardarRequestDTO
//
// Este mismo modelo se utiliza para:
// - Validar condiciones digitadas.
// - Guardar condiciones definitivas.
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

}

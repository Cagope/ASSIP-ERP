// Modelos alineados con SolicitudFormalizacionDetalleDTO y GuardarRequestDTO.
export interface SolicitudFormalizacionDetalle {
  idSolicitudCredito: number;
  numeroSolicitud: string;
  idAgencia: number;
  nombreAgencia: string;
  idDatosPersonal: number;
  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;
  idSolicitudProceso: number;
  nombreProceso: string;
  idSolicitudResultado: number;
  nombreResultado: string;
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
  codigoGarantiaCredito: string | null;
  nombreGarantiaCredito: string | null;
  idFondoGarantia: number | null;
  nombreFondoGarantia: string | null;
  idEnteAprobacion: number | null;
  fechaDecision: string | null;
  observacionesAprobacion: string | null;
  numeroActa: string | null;
  fechaActa: string | null;
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
  condicionesModificadas: boolean | null;
  fechaFinAprobacion: string | null;
  fechaFinFormalizacion: string | null;
}

export interface SolicitudFormalizacionGuardarRequest {
  valorFormalizado: number;
  plazoFormalizado: number;
  codigoFormaPagoFormalizada: string;
  periodoCodigoInteresFormalizado: string;
  tipoModalidadInteresFormalizado: string;
  amortizacionCapitalFormalizada: number;
  codigoTipoCuotaFormalizada: string;
  mesesGraciaCapitalFormalizados: 0;
  mesesGraciaInteresFormalizados: 0;
  tasaNominalFormalizada: number;
}

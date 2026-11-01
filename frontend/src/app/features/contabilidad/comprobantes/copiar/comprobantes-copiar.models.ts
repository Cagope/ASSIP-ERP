/** Solicitud del nuevo comprobante sin modificar los movimientos del origen. */
export interface ComprobanteCopiarRequest {
  idAgenciaOrigen: number;
  tipoComprobanteOrigen: string;
  numeroComprobanteOrigen: string;
  idAgenciaDestino: number;
  tipoComprobanteDestino: string;
  fechaContableDestino: string;
  conceptoComprobanteDestino: string;
}
/** Identificación del comprobante generado por el backend. */
export interface ComprobanteCopiadoResponse {
  idAgencia: number;
  tipoComprobante: string;
  numeroComprobante: string;
  mensaje: string;
}

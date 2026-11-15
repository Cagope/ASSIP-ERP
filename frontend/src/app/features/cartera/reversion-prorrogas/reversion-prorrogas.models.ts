/** Contratos del controlador de Reversión de Prórrogas V1. */
export interface ProrrogaResumen {
  idProrroga: number;
  numeroProrroga: number;
  estado: string;
  fechaAplicacion: string | null;
  valorTotal: number;
  tipoComprobante: string | null;
  numeroComprobante: string | null;
  capitalAnterior: string | null;
  capitalNueva: string | null;
  interesAnterior: string | null;
  interesNueva: string | null;
  vencimientoAnterior: string | null;
  vencimientoNueva: string | null;
}
export interface ConfirmarReversionProrrogaRequest {
  fechaReversion: string;
  motivo: string;
}
export interface ResultadoReversionProrroga {
  idProrroga: number;
  idPago: number;
  numeroComprobante: string;
  capitalRestituido: number;
  ahorrosDevueltos: number;
}

/** Resultado preliminar de consulta, sin modificar movimientos financieros. */
export interface VistaPreviaReversionProrroga {
  idProrroga: number;
  idPago: number | null;
  comprobanteOriginal: string | null;
  valorTotal: number | null;
  capitalARestituir: number;
  ahorrosADevolver: number;
  cajaPorRegularizar: number;
  tesoreriaPorRegularizar: number;
  elegiblePreliminar: boolean;
  impedimentos: string[];
}

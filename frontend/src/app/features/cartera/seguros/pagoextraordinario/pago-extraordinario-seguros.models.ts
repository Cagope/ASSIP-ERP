/** Formas de disminución admitidas por el servicio de Seguros. */
export type PagoSeguroForma = 'CAJA' | 'DEPOSITOS' | 'BANCOS' | 'CORRECCION';

export interface PagoSeguroMedio {
  forma: PagoSeguroForma;
  valor: number;
  idCaja: number | null;
  idCuentaAhorro: number | null;
}
export interface PagoSeguroSolicitud {
  idCredito: number;
  fechaMovimiento: string;
  medios: PagoSeguroMedio[];
  justificacion: string;
}
export interface PagoSeguroCuentaAhorro {
  idCuentaAhorro: number;
  idAgencia: number;
  codigoCuenta: string;
  nombreForma: string;
  saldoDisponible: number;
}
export interface PagoSeguroLinea {
  idCatalogoCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;
  debito: number;
  credito: number;
  detalle: string;
  idTercero: number;
  documentoTercero: string;
}
export interface PagoSeguroVistaPrevia {
  idCredito: number;
  idAgencia: number;
  tipoComprobante: string;
  numeroSugerido: string;
  saldoAnterior: number;
  totalAplicado: number;
  saldoPosterior: number;
  lineas: PagoSeguroLinea[];
}
export interface PagoSeguroResultado {
  idDetalleSeguro: number;
  tipoComprobante: string;
  numeroComprobante: string;
  saldoPosterior: number;
  movimientosCaja: number[];
  operacionTesoreria: number | null;
}

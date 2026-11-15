/** Formas de disminución admitidas por el servicio de Costass. */
export type PagoCostasForma = 'CAJA' | 'DEPOSITOS' | 'BANCOS' | 'CORRECCION';

export interface PagoCostasMedio {
  forma: PagoCostasForma;
  valor: number;
  idCaja: number | null;
  idCuentaAhorro: number | null;
}
export interface PagoCostasSolicitud {
  idCredito: number;
  fechaMovimiento: string;
  medios: PagoCostasMedio[];
  justificacion: string;
}
export interface PagoCostasCuentaAhorro {
  idCuentaAhorro: number;
  idAgencia: number;
  codigoCuenta: string;
  nombreForma: string;
  saldoDisponible: number;
}
export interface PagoCostasLinea {
  idCatalogoCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;
  debito: number;
  credito: number;
  detalle: string;
  idTercero: number;
  documentoTercero: string;
}
export interface PagoCostasVistaPrevia {
  idCredito: number;
  idAgencia: number;
  tipoComprobante: string;
  numeroSugerido: string;
  saldoAnterior: number;
  totalAplicado: number;
  saldoPosterior: number;
  lineas: PagoCostasLinea[];
}
export interface PagoCostasResultado {
  idDetalleCostas: number;
  tipoComprobante: string;
  numeroComprobante: string;
  saldoPosterior: number;
  movimientosCaja: number[];
  operacionTesoreria: number | null;
}

/** Cargo original y contrapartida contable identificados por su comprobante. */
export interface PagoCostasCargo {
  idDetalleCargo: number;
  fecha: string;
  valor: number;
  observacion: string;
  idCuentaContrapartida: number;
  idTercero: number | null;
  codigoCuenta: string;
  nombreCuenta: string;
  documentoTercero: string;
  valorCorregido: number;
}


/** Datos sugeridos por el backend para una causación extraordinaria de seguros. */
export interface CausacionExtraordinariaSegurosSugerencia {
  idCredito: number;
  idCreditoSeguro: number;
  idAgencia: number;
  fechaSugerida: string;
  saldoBase: number;
  porcentajeBaseSeguro: number;
  porcentajeExtraprima: number;
  porcentajeAplicar: number;
  valorSugerido: number;
  saldoSeguro: number;
}

/** Solicitud de vista previa o confirmación; los porcentajes se toman del backend. */
export interface CausacionExtraordinariaSegurosSolicitud {
  idCredito: number;
  fechaMovimiento: string;
  saldoBase: number;
  valor: number;
  justificacion: string;
}

/** Partida de la nota contable, con cuenta y tercero legibles. */
export interface CausacionExtraordinariaSegurosLinea {
  codigoCuenta: string;
  nombreCuenta: string;
  documentoTercero: string;
  debito: number;
  credito: number;
}

/** Nota contable propuesta, sin movimientos financieros registrados. */
export interface CausacionExtraordinariaSegurosVistaPrevia {
  idCredito: number;
  idAgencia: number;
  tipoComprobante: string;
  numeroSugerido: string;
  fechaMovimiento: string;
  saldoBase: number;
  porcentajeBaseSeguro: number;
  porcentajeExtraprima: number;
  porcentajeAplicar: number;
  valorSugerido: number;
  valor: number;
  justificacion: string;
  movimientos: CausacionExtraordinariaSegurosLinea[];
}

/** Confirmación del cargo de seguro y del comprobante NC generado. */
export interface CausacionExtraordinariaSegurosResultado {
  idDetalle: number;
  tipoComprobante: string;
  numeroComprobante: string;
  saldoSeguro: number;
}

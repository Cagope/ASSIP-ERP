
/**
 * CARTERA - REVERSIÓN DE PAGOS DE CUOTAS V1.
 * Contratos correspondientes a los record del backend.
 * BigDecimal: number. LocalDate: string (yyyy-MM-dd).
 */

/** Pago original registrado en Cartera. */
export interface ReversionPagoOriginal {
  idPago: number;
  idCredito: number;
  idAgencia: number;
  fechaPago: string | null;
  fechaContable: string | null;
  valor: number;
  estado: string;
  mediosRecaudoJson: string | null;
}

/** Referencias financieras y contables del pago seleccionado. */
export interface ReversionPagoDetalle extends ReversionPagoOriginal {
  idExtractoCartera: number | null;
  tipoComprobante: string | null;
  numeroComprobante: string | null;
  idMovimientoCaja: number | null;
}

/** Elegibilidad preliminar determinada por el backend. */
export interface ReversionVistaPrevia {
  pago: ReversionPagoDetalle;
  ultimoPagoAplicado: number | null;
  elegibleParaAnalisis: boolean;
  observacion: string;
}

/** Contrapartida contable del comprobante original, con datos descriptivos. */
export interface ReversionLineaContable {
  idCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;
  idTercero: number | null;
  documentoTercero: string | null;
  nombreTercero: string | null;
  debito: number;
  credito: number;
  detalleOriginal: string | null;
}

/** Comprobante contrario proyectado por el backend. */
export interface ReversionComprobanteContrario {
  idPago: number;
  tipoOriginal: string;
  numeroOriginal: string;
  totalDebitos: number;
  totalCreditos: number;
  lineas: ReversionLineaContable[];
}

/** Estado de restitución validado en Cartera. */
export interface ReversionEstadoRestitucion {
  consistente: boolean;
  observacion: string;
  saldoActual: number;
  saldoRestituible: number;
}

/** Solicitud de confirmación de reversión. */
export interface ConfirmarReversionRequest {
  fechaReversion: string;
  motivo: string;
}

/** Resultado definitivo de la transacción de reversión. */
export interface ReversionResultado {
  idPago: number;
  numeroComprobante: string;
  saldoRestituido: number;
  valorAhorros: number;
  valorCajaPendiente: number;
}

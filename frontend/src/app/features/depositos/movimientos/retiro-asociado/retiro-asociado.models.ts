
/**
 * Retiro integral del asociado.
 *
 * Los saldos provienen del backend.
 * El frontend no realiza cálculos contables.
 */

export type EstadoFinalRetiro = 'C' | 'R';

export type DestinoPagoRetiro = 'CAJA' | 'CXP';

/**
 * Cuenta de ahorro del asociado.
 *
 * Corresponde al record CuentaRetiro
 * de RetiroAsociadoRepository.
 *
 * El proceso contempla las cuentas pendientes
 * de retiro, incluyendo aquellas con saldo cero.
 */
export interface RetiroAsociadoCuenta {
  idCuentaAhorro: number;
  idDatosPersonal: number;
  idAgencia: number;
  idFormaAhorro: number;

  codigoCuenta: string;
  codigoForma: string;
  nombreForma: string;
  tipoCaptacion: string;

  saldoActual: number;

  estadoCuenta: string | null;
  estadoOperativo: boolean | null;
  fechaEstadoCuenta: string | null;
}

/**
 * Medio de pago del retiro.
 */
export interface RetiroAsociadoMedioPago {
  destino: DestinoPagoRetiro;
  valor: number;
}

/**
 * Distribución del retiro por cuenta.
 */
export interface RetiroAsociadoDistribucion {
  idCuentaAhorro: number;
  valorCaja: number;
  valorCxp: number;
}

/**
 * Solicitud del retiro integral.
 *
 * Incluye los datos administrativos y
 * la distribución financiera del retiro.
 */
export interface RetiroAsociadoEntrada {
  idDatosPersonal: number;
  idAgenciaOperacion: number;
  idCaja: number | null;

  fechaProceso: string;
  estadoFinal: EstadoFinalRetiro;
  comentarioCuentas: string;

  numeroActa: string | null;
  fechaActa: string | null;

  mediosPago: RetiroAsociadoMedioPago[];
  distribuciones: RetiroAsociadoDistribucion[];
}

/**
 * Movimiento contable de la vista previa.
 */
export interface RetiroAsociadoMovimientoPreview {
  codigoCuenta: string;
  nombreCuenta: string;
  detalle: string;
  valorDebito: number;
  valorCredito: number;
}

/**
 * Comprobante contable de la vista previa.
 */
export interface RetiroAsociadoComprobantePreview {
  idAgencia: number;
  nombreAgencia: string;
  tipoComprobante: string;
  concepto: string;

  movimientos: RetiroAsociadoMovimientoPreview[];

  totalDebito: number;
  totalCredito: number;
}

/**
 * Resultado de la validación previa.
 *
 * Corresponde a RetiroAsociadoPreviewDTO.
 */
export interface RetiroAsociadoPreview {
  idDatosPersonal: number;
  idAgenciaOrigen: number;

  cuentas: RetiroAsociadoCuenta[];

  totalSaldos: number;
  totalCaja: number;
  totalCxp: number;
  diferencia: number;

  comprobantes: RetiroAsociadoComprobantePreview[];

  numeroActa: string | null;
  fechaActa: string | null;
}

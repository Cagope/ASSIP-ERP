
/**
 * ASSIP ERP
 * Tesorería - Movimientos de Fondos
 *
 * Modelos para consignaciones, retiros,
 * traslados entre cajas y traslados bancarios.
 */

export type TipoMovimientoFondo =
  | 'CONSIGNACION'
  | 'RETIRO'
  | 'TRASLADO_CAJAS'
  | 'TRASLADO_BANCARIO';

export type ModalidadTrasladoCaja =
  | 'PROVISION'
  | 'REEMBOLSO'
  | 'TRASLADO';

export interface EfectivoCaja {
  idCaja: number;
  valor: number;
}

export interface MovimientoFondoRequest {
  tipoMovimiento: TipoMovimientoFondo;
  idAgencia: number;
  fechaMovimiento: string;
  numeroSoporte: string;
  observacion?: string | null;
  valor?: number | null;

  idCuentaBancariaOrigen?: number | null;
  idCuentaBancariaDestino?: number | null;

  idCajaOrigen?: number | null;
  idCajaDestino?: number | null;

  modalidadTrasladoCaja?: ModalidadTrasladoCaja | null;

  idsCheques?: number[];
  efectivos?: EfectivoCaja[];
}

export interface MovimientoContablePreview {
  idAgencia: number;
  fechaAuxiliar: string;
  idCatalogoCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;
  detalleMovimiento: string;
  valorDebito: number;
  valorCredito: number;
}

export interface MovimientoFondoPreview {
  tipoMovimiento: TipoMovimientoFondo;
  modalidadTrasladoCaja: ModalidadTrasladoCaja | null;

  idAgencia: number;
  fechaMovimiento: string;
  numeroSoporte: string;

  idCuentaBancariaOrigen: number | null;
  idCuentaBancariaDestino: number | null;

  idCajaOrigen: number | null;
  idCajaDestino: number | null;

  tipoComprobante: string;
  conceptoComprobante: string;

  movimientos: MovimientoContablePreview[];

  totalDebito: number;
  totalCredito: number;
  diferencia: number;
  cuadrado: boolean;

  valorCheques: number;
  valorEfectivo: number;
  valorTotal: number;
}

export interface MovimientoFondoResponse {
  idMovimientoFondo: number | null;
  tipoMovimiento: TipoMovimientoFondo;
  idAgencia: number;
  fechaMovimiento: string;
  numeroSoporte: string;
  observacion: string | null;

  idCuentaBancariaOrigen: number | null;
  codigoBancoOrigen: number | null;
  nombreBancoOrigen: string | null;
  numeroCuentaOrigen: string | null;
  nombreCuentaOrigen: string | null;

  idCuentaBancariaDestino: number | null;
  codigoBancoDestino: number | null;
  nombreBancoDestino: string | null;
  numeroCuentaDestino: string | null;
  nombreCuentaDestino: string | null;

  idCajaOrigen: number | null;
  nombreCajaOrigen: string | null;
  idCajaDestino: number | null;
  nombreCajaDestino: string | null;

  modalidadTrasladoCaja: ModalidadTrasladoCaja | null;

  cantidadCheques: number;
  valorCheques: number;
  valorEfectivo: number;
  valorTotal: number;

  tipoComprobante: string;
  numeroComprobante: string;
  conceptoComprobante: string;
}

export interface CuentaBancariaMovimiento {
  idCuentaBancaria: number;
  codigoBanco: number;
  nombreBanco: string;
  numeroCuenta: string;
  nombreCuenta: string;

  idCuentaContable: number;
  codigoCuentaContable: string;
  nombreCuentaContable: string;
}

export interface CajaMovimiento {
  idCaja: number;
  idAgencia: number;
  codigoCaja: string;
  nombreCaja: string;
  tipoCaja: string;

  idCatalogoCuentaCaja: number;
  codigoCuentaContableCaja: string;
  nombreCuentaContableCaja: string;
}

export interface CajaConProvision {
  idCaja: number;
  idAgencia: number;
  codigoCaja: string;
  nombreCaja: string;
  idProvision: number;

  idCuentaContableCaja: number;
  codigoCuentaContableCaja: string;
  nombreCuentaContableCaja: string;
  efectivoDisponible: number;
}

export interface ChequeDisponible {
  idChequeRecibido: number;
  idAgencia: number;
  idCaja: number;

  numeroCheque: string;
  valorCheque: number;

  codigoCaja: string;

  idCuentaContableCaja: number;
  codigoCuentaContableCaja: string;
  nombreCuentaContableCaja: string;
}

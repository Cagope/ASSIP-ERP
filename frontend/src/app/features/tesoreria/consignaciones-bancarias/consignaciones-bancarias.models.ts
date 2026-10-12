export interface CuentaBancariaConsignacion {
  idCuentaBancaria: number;
  codigoBanco: number;
  nombreBanco: string;
  numeroCuenta: string;
  nombreCuenta: string;
  codigoTipoCuenta: string;
  nombreTipoCuenta: string;
  idCuentaContable: number;
  codigoCuentaContable: string;
  nombreCuentaContable: string;
}

export interface ChequeDisponible {
  idChequeRecibido: number;
  idMovimientoCajaOrigen: number;
  idCaja: number;
  codigoCaja: string;
  nombreCaja: string;
  idAgencia: number;
  idCuentaContableCaja: number;
  codigoCuentaContableCaja: string;
  nombreCuentaContableCaja: string;
  fechaRecibido: string;
  codigoBancoCheque: number;
  nombreBancoCheque: string;
  numeroCheque: string;
  valorCheque: number;
  numeroDocumentoOrigen: string | null;
  codigoReferencia: string | null;
  moduloOrigen: string | null;
  procesoOrigen: string | null;
  observacion: string | null;
}

export interface CajaEfectivoDisponible {
  idCaja: number;
  idAgencia: number;
  codigoCaja: string;
  nombreCaja: string;
  idCuentaContableCaja: number;
  codigoCuentaContableCaja: string;
  nombreCuentaContableCaja: string;
  idProvision: number;
  fechaContable: string;
}

export interface EfectivoCajaRequest {
  idCaja: number;
  valor: number;
}

export interface ConsignacionBancariaRequest {
  idAgencia: number;
  idCuentaBancaria: number;
  fechaConsignacion: string;
  numeroConsignacion: string;
  observacion: string | null;
  idsCheques: number[];
  efectivos: EfectivoCajaRequest[];
}

// =========================================================
// PREVIEW CONTABLE
// =========================================================

export interface MovimientoContablePreview {
  idCatalogoCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;
  detalleMovimiento: string;
  valorDebito: number;
  valorCredito: number;
}

export interface ConsignacionBancariaPreviewResponse {
  movimientos: MovimientoContablePreview[];

  totalDebito: number;
  totalCredito: number;
  diferencia: number;
  cuadrado: boolean;

  valorCheques: number;
  valorEfectivo: number;
  valorTotal: number;

  conceptoComprobante: string;
}

// =========================================================
// RESULTADO DEFINITIVO
// =========================================================

export interface ConsignacionBancariaResponse {
  idAgencia: number;
  fechaConsignacion: string;
  numeroConsignacion: string;
  idCuentaBancaria: number;
  codigoBanco: number;
  nombreBanco: string;
  numeroCuenta: string;
  nombreCuenta: string;
  cantidadCheques: number;
  valorCheques: number;
  valorEfectivo: number;
  valorTotal: number;
  tipoComprobante: string;
  numeroComprobante: string;
  conceptoComprobante: string;
}

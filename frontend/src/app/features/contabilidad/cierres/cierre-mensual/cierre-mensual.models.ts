/** Contratos del backend de cierre mensual contable. */
export interface CierreMensualDiaPendiente { fecha: string; estado: string; }
export interface CierreMensualResumen {
  idAgencia: number; nombreAgencia: string; ano: number; mes: number;
  estadoPeriodo: string; puedeCerrar: boolean; mensajeValidacion: string;
  cantidadDiasMes: number; cantidadDiasCerrados: number; cantidadDiasPendientes: number;
  diasPendientes: CierreMensualDiaPendiente[]; cantidadCuentas: number; cantidadMovimientos: number;
  totalDebitoAnterior: number; totalCreditoAnterior: number;
  totalDebitoMes: number; totalCreditoMes: number;
  totalDebitoAcumulado: number; totalCreditoAcumulado: number; diferenciaMes: number;
}
export interface CierreMensualCuenta {
  idCatalogoCuenta: number; codigoCuenta: string; nombreCuenta: string;
  naturalezaCuenta: string; nivelCuenta: number; cuentaOperable: boolean;
  debitoAnterior: number; creditoAnterior: number; saldoAnterior: number;
  debitoMes: number; creditoMes: number; debitoAcumulado: number;
  creditoAcumulado: number; saldoFinal: number;
}

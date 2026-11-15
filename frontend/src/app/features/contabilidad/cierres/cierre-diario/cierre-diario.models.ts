/** Contratos del backend de cierre diario. */
export interface CierreDiarioRequest {
  idAgencia: number;
  fecha: string;
  justificacion?: string | null;
}

export interface CierreDiarioDetalleCuenta {
  idCatalogoCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;
  totalDebitos: number;
  totalCreditos: number;
  cantidadMovimientos: number;
}

export interface CierreDiarioResumen {
  idAgencia: number;
  fecha: string;
  estadoPeriodoMensual: string;
  estadoDia: 'A' | 'C';
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
  cantidadComprobantes: number;
  cantidadMovimientos: number;
  detalleCuentas: CierreDiarioDetalleCuenta[];
}

/**
 * Representa un día calendario dentro del control mensual.
 * Incluye su estado contable, valores y advertencias.
 */
export interface CierreDiarioDia {
  fecha: string;
  estado: 'A' | 'C';
  cantidadComprobantes: number;
  cantidadMovimientos: number;
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
  tieneMovimientos: boolean;
  cuadrado: boolean;
  advertencia: string | null;
}

/**
 * Consolida los cierres diarios de una agencia para un mes.
 * Incluye todos los días calendario, incluso sin movimientos.
 */
export interface CierreDiarioMensual {
  idAgencia: number;
  ano: number;
  mes: number;
  estadoPeriodoMensual: string;
  diasAbiertos: number;
  diasCerrados: number;
  diasConDiferencias: number;
  diasSinMovimientos: number;
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
  dias: CierreDiarioDia[];
}


/**
 * Representa un movimiento contable individual del informe diario.
 * Conserva los datos necesarios para impresión y exportación a Excel.
 */
export interface CierreDiarioInformeMovimiento {
  idAuxiliarContable: number;
  tipoComprobante: string;
  numeroComprobante: string;
  codigoCuenta: string;
  nombreCuenta: string;
  documentoTercero: string | null;
  nombreTercero: string | null;
  detalleMovimiento: string | null;
  valorDebito: number;
  valorCredito: number;
  origenModulo: string | null;
  documentoOrigen: string | null;
  documentoSoporte: string | null;
}

/**
 * Contrato del informe contable de una agencia y fecha.
 * La impresión y el Excel utilizan la misma información.
 */
export interface CierreDiarioInforme {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
  fecha: string;
  estadoDia: 'A' | 'C';
  estadoPeriodoMensual: string;
  fechaGeneracion: string;
  cantidadComprobantes: number;
  cantidadMovimientos: number;
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
  resumenCuentas: CierreDiarioDetalleCuenta[];
  movimientos: CierreDiarioInformeMovimiento[];
}

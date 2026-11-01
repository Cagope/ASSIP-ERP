
/**
 * Filtros para consultar el auxiliar contable por cuentas.
 * La agencia debe estar autorizada para el usuario autenticado.
 */
export interface AuxiliarCuentasFiltro {
  idAgencia: number;
  cuentaInicial: string;
  cuentaFinal: string;
  fechaInicial: string;
  fechaFinal: string;
}

/**
 * Movimiento contable individual con identificación del tercero,
 * valores, saldo acumulado y trazabilidad para revisión y auditoría.
 */
export interface AuxiliarCuentaMovimiento {
  idAuxiliarContable: number;
  fechaAuxiliar: string;
  tipoComprobante: string | null;
  numeroComprobante: string | null;
  detalleMovimiento: string | null;
  estadoMovimiento: string | null;
  idDatosPersonal: number | null;
  documentoTercero: string | null;
  nombreTercero: string | null;
  valorDebito: number;
  valorCredito: number;
  valorBase: number;
  saldoAcumulado: number;
  fkSeguridadCreacion: number;
  usernameCreacion: string | null;
  nombreUsuarioCreacion: string | null;
  fechaCreacion: string | null;
  fkSeguridadEdicion: number;
  usernameEdicion: string | null;
  nombreUsuarioEdicion: string | null;
  fechaEdicion: string | null;
  origenModulo: string | null;
  documentoOrigen: string | null;
  documentoSoporte: string | null;
}

/**
 * Resultado de una cuenta contable operable.
 * Contiene saldo inicial, movimientos, totales y saldo final.
 */
export interface AuxiliarCuenta {
  idCatalogoCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;
  naturalezaCuenta: 'D' | 'C';
  saldoInicial: number;
  movimientos: AuxiliarCuentaMovimiento[];
  totalDebitos: number;
  totalCreditos: number;
  saldoFinal: number;
}

/**
 * Respuesta completa del informe de auxiliares contables.
 * Agrupa las cuentas operables dentro del rango y período consultados.
 */
export interface AuxiliarCuentasResponse {
  idAgencia: number;
  cuentaInicial: string;
  cuentaFinal: string;
  fechaInicial: string;
  fechaFinal: string;
  cuentas: AuxiliarCuenta[];
}

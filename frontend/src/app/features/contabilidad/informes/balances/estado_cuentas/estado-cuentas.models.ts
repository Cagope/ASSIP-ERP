/** Contratos del informe de saldos hasta una fecha de corte. */
export interface EstadoCuentasFiltro {
  idAgencia: number;
  fechaCorte: string;
  codigoCuentaPrincipal: string;
  nivel: number;
  incluirFirmas: boolean;
}

export interface EstadoCuentasFila {
  codigoCuenta: string;
  nombreCuenta: string;
  nivelCuenta: number;
  naturalezaCuenta: string;
  cuentaOperable: boolean;
  saldoAnterior: number;
  valorDebitoMes: number;
  valorCreditoMes: number;
  saldoFinal: number;
}

export interface EstadoCuentasTotales {
  saldoAnterior: number;
  valorDebitoMes: number;
  valorCreditoMes: number;
  saldoFinal: number;
}

export interface EstadoCuentasFirmante {
  titulo: string;
  nombre: string;
  tarjetaProfesional: string | null;
}

export interface EstadoCuentasFirmas {
  firmante1: EstadoCuentasFirmante | null;
  firmante2: EstadoCuentasFirmante | null;
  firmante3: EstadoCuentasFirmante | null;
}

export interface EstadoCuentasRespuesta extends EstadoCuentasFiltro {
  codigoAgencia: string;
  nombreAgencia: string;
  filas: EstadoCuentasFila[];
  totales: EstadoCuentasTotales;
  firmas: EstadoCuentasFirmas | null;
}

export interface CuentaPrincipal {
  codigo: string;
  nombre: string;
}

export interface EstadoCuentasMovimientosFiltro {
  idAgencia: number;
  fechaCorte: string;
  codigoCuenta: string;
  pagina: number;
  tamanoPagina: number;
}

export interface EstadoCuentasMovimiento {
  idAuxiliarContable: number;
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
  idCatalogoCuenta: number;
  fechaAuxiliar: string;
  tipoComprobante: string;
  numeroComprobante: string;
  idDatosPersonal: number | null;
  detalleMovimiento: string;
  valorDebito: number;
  valorCredito: number;
}

export interface EstadoCuentasMovimientosRespuesta {
  codigoCuenta: string;
  ano: number;
  mes: number;
  pagina: number;
  tamanoPagina: number;
  hayMas: boolean;
  movimientos: EstadoCuentasMovimiento[];
}

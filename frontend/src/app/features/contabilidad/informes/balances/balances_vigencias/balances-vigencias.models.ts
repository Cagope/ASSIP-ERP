/** Contratos del informe de saldos hasta una fecha de corte. */
export interface BalancesVigenciasFiltro {
  idAgencia: number;
  ano: number;
  nivel: number;
  incluirFirmas: boolean;
}

export interface BalancesVigenciasFila {
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

export interface BalancesVigenciasTotales {
  saldoAnterior: number;
  valorDebitoMes: number;
  valorCreditoMes: number;
  saldoFinal: number;
}

export interface BalancesVigenciasFirmante {
  titulo: string;
  nombre: string;
  tarjetaProfesional: string | null;
}

export interface BalancesVigenciasFirmas {
  firmante1: BalancesVigenciasFirmante | null;
  firmante2: BalancesVigenciasFirmante | null;
  firmante3: BalancesVigenciasFirmante | null;
}

export interface BalancesVigenciasRespuesta extends BalancesVigenciasFiltro {
  codigoAgencia: string;
  nombreAgencia: string;
  filas: BalancesVigenciasFila[];
  totales: BalancesVigenciasTotales;
  firmas: BalancesVigenciasFirmas | null;
}

export interface CuentaPrincipal {
  codigo: string;
  nombre: string;
}

export interface BalancesVigenciasMovimientosFiltro {
  idAgencia: number;
  ano: number;
  codigoCuenta: string;
  pagina: number;
  tamanoPagina: number;
}

export interface BalancesVigenciasMovimiento {
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

export interface BalancesVigenciasMovimientosRespuesta {
  codigoCuenta: string;
  ano: number;
  mes: number;
  pagina: number;
  tamanoPagina: number;
  hayMas: boolean;
  movimientos: BalancesVigenciasMovimiento[];
}

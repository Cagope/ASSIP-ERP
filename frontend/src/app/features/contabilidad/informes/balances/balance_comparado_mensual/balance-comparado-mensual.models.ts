/** Contratos de consulta del Balance Comparado Mensual. */
export interface BalanceComparadoMensualFiltro {
  idAgencia: number;
  anoPeriodo1: number;
  mesPeriodo1: number;
  anoPeriodo2: number;
  mesPeriodo2: number;
  nivel: number;
  soloBalance: boolean;
  incluirFirmas: boolean;
}
export interface BalanceFila {
  codigoCuenta: string;
  nombreCuenta: string;
  nivelCuenta: number;
  naturalezaCuenta: string;
  cuentaOperable: boolean;
  saldoPeriodo1: number;
  saldoPeriodo2: number;
  variacionValor: number;
  variacionPorcentaje: number | null;
}
export interface BalanceTotales {
  saldoPeriodo1: number;
  saldoPeriodo2: number;
  variacionValor: number;
  variacionPorcentaje: number | null;
}
export interface BalanceFirmante { titulo: string; nombre: string; tarjetaProfesional: string | null; }
export interface BalanceFirmas {
  firmante1: BalanceFirmante | null;
  firmante2: BalanceFirmante | null;
  firmante3: BalanceFirmante | null;
}
export interface BalanceComparadoMensualRespuesta extends BalanceComparadoMensualFiltro {
  codigoAgencia: string;
  nombreAgencia: string;
  filas: BalanceFila[];
  totales: BalanceTotales;
  firmas: BalanceFirmas | null;
}

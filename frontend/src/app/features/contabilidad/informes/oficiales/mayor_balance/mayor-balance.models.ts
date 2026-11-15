export interface MayorBalanceFiltro { idAgencia: number; ano: number; mes: number; acumuladoAnual: boolean; }
export type MayorBalanceFormato = 'INTERNO' | 'OFICIAL';
export interface MayorBalanceFila {
  codigo: string; nombre: string; naturaleza: string;
  debitoAnterior: number; creditoAnterior: number;
  debitoMovimiento: number; creditoMovimiento: number;
  debitoSaldo: number; creditoSaldo: number;
}
export interface MayorBalanceTotales {
  debitoAnterior: number; creditoAnterior: number;
  debitoMovimiento: number; creditoMovimiento: number;
  debitoSaldo: number; creditoSaldo: number;
}
export interface MayorBalanceResultado extends MayorBalanceFiltro {
  codigoAgencia: string; nombreAgencia: string; nivel: number;
  desde: string; hasta: string; filas: MayorBalanceFila[]; totales: MayorBalanceTotales;
  diferenciaMovimiento: number; diferenciaSaldo: number;
}

export interface CajaDiarioFiltro {
  idAgencia: number;
  ano: number;
  mes: number;
  nivel: number;
  acumuladoAnual: boolean;
}
export interface CajaDiarioFila {
  fecha: string | null;
  codigo: string;
  nombre: string;
  debito: number;
  credito: number;
}
export interface CajaDiarioTotalDia { fecha: string; debito: number; credito: number; }
export interface CajaDiarioResultado extends CajaDiarioFiltro {
  codigoAgencia: string;
  nombreAgencia: string;
  desde: string;
  hasta: string;
  diario: CajaDiarioFila[];
  totalesDias: CajaDiarioTotalDia[];
  resumen: CajaDiarioFila[];
  totalDebito: number;
  totalCredito: number;
  diferencia: number;
}
export type CajaDiarioFormato = 'INTERNO' | 'OFICIAL';

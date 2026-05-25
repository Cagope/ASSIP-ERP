export interface SimuladorBusquedaFiltros {
  documento: string;
  nombres: string;
  primer_apellido: string;
  segundo_apellido: string;
}

export interface SimuladorAsociado {
  id_datos_personal: number;
  id_agencia?: number;
  documento: string;
  nombre_completo: string;
  agencia?: string;
  codigo_cuenta?: string;
  nombre_forma_ahorro?: string;
  nombre_estado?: string;
  saldo_actual_cuenta?: number;
}

export interface SimuladorCdatModel {
  valorCdat: number | null;
  tasaNominalAnual: number | null;
  tasaEfectivaAnual?: number | null;
  fechaApertura: string;
  plazoMeses: number | null;
  fechaVencimiento: string;
  formaPagoInteres: 'MENSUAL' | 'VENCIMIENTO';
  aplicaRetencion: boolean;
  baseRetencion: number | null;
  porcentajeRetencion: number | null;
}

export interface SimuladorFlujoItem {
  periodo: number;
  fechaPago: string;
  dias: number;
  capital: number;
  interesBruto: number;
  baseRetencion?: number;
  baseRetencionAplicada?: number;
  porcentajeRetencion: number;
  valorRetencion: number;
  interesNeto: number;
  pagoCliente: number;
  saldoFinal: number;
}

export interface SimuladorResumen {
  capital: number;
  totalInteresBruto: number;
  totalRetencion: number;
  totalInteresNeto: number;
  totalPagoCliente: number;
  valorAlVencimiento: number;
}

export interface SimuladorCdatEntrada {
  idAgencia: number;
  valorCdat: number;
  tasaNominalAnual: number;
  fechaApertura: string;
  plazoMeses: number;
  formaPagoInteres: 'MENSUAL' | 'VENCIMIENTO';
  aplicaRetencion: boolean;
}

export interface SimuladorCdatPreview {
  capital: number;
  tasaNominalAnual: number;
  baseRetencion: number;
  porcentajeRetencion: number;
  fechaApertura: string;
  fechaVencimiento: string;
  plazoMeses: number;
  formaPagoInteres: 'MENSUAL' | 'VENCIMIENTO';
  totalInteresBruto: number;
  totalRetencion: number;
  totalInteresNeto: number;
  totalPagoCliente: number;
  valorAlVencimiento: number;
  items: SimuladorFlujoItem[];
}

export type FechasCdatTipoInforme =
  | 'NUEVOS'
  | 'CANCELADOS'
  | 'VENCER'
  | 'VENCIDOS'
  | 'RENOVADOS';

export interface FechasCdatRequest {
  tipoInforme: FechasCdatTipoInforme;
  fechaInicial: string;
  fechaFinal: string;
}

export interface FechasCdatResumen {
  cantidad: number;
  valorTotal: number;
  promedioTasa: number;
  promedioPlazo: number;
}

export interface FechasCdatItem {
  idCuentaCdat: number;
  codigoCdat: string;
  documento: string;
  nombreCompleto: string;
  agencia: string;
  fechaApertura: string;
  fechaVencimiento: string;
  fechaCancelacion?: string | null;
  fechaRenovacion?: string | null;
  plazoMeses: number;
  tasa: number;
  valor: number;
  estado: string;
}

export interface FechasCdatResponse {
  resumen: FechasCdatResumen;
  resultados: FechasCdatItem[];
}

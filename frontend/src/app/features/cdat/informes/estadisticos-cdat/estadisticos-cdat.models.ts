export interface EstadisticosCdatRequest {
  fechaCorteActual: string;
  fechaCorteAnterior?: string | null;
}

export interface EstadisticosCdatResumen {
  totalCdats: number;
  valorTotalCaptado: number;
  promedioTasa: number;
  promedioPlazo: number;
  vencen30Dias: number;
}

export interface EstadisticosCdatGrupo {
  concepto: string;
  cantidad: number;
  valorTotal: number;
  promedioTasa: number;
  promedioPlazo: number;
  participacion: number;
}

export interface EstadisticosCdatTasa {
  tasa: string;
  cantidad: number;
  valorTotal: number;
  promedioPlazo: number;
  participacion: number;
}

export interface EstadisticosCdatResponse {
  resumen: EstadisticosCdatResumen;
  rangos: EstadisticosCdatGrupo[];
  amortizacion: EstadisticosCdatGrupo[];
  plazos: EstadisticosCdatGrupo[];
  plazosDetalle: EstadisticosCdatGrupo[];
  tasas: EstadisticosCdatTasa[];
  tasasDetalle: EstadisticosCdatTasa[];
}

export interface EstadisticosCdatDetalleRequest {
  fechaCorte: string;
  tipoBloque: string;
  concepto: string;
}

export interface EstadisticosCdatDetalle {
  idCuentaCdat: number;
  codigoCdat: string;
  documento: string;
  nombreCompleto: string;
  agencia: string;
  fechaAperturaCdat: string;
  fechaVencimientoCdat: string;
  plazoMeses: number;
  tasaNominalAnual: number;
  saldoActualCdat: number;
  estadoCdat: string;
}

export interface EstadisticosCdatBloqueDetalleItem<TResumen> {
  concepto: string;
  resumen: TResumen;
  detalle: EstadisticosCdatDetalle[];
}

export interface EstadisticosCdatBloqueCompleto<TResumen> {
  titulo: string;
  tipoBloque: string;
  fechaCorte: string;
  grupos: EstadisticosCdatBloqueDetalleItem<TResumen>[];
}

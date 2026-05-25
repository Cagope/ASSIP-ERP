export interface DashboardCdatRequest {
  fechaCorte: string;
}

export interface DashboardCdatResumen {
  totalCdats: number;
  valorTotalCaptado: number;
  promedioTasa: number;
  promedioPlazo: number;
  vencen30Dias: number;
  renovacionesMes: number;
  cancelacionesMes: number;
}

export interface DashboardCdatAgencia {
  agencia: string;
  cantidad: number;
  valorTotal: number;
  promedioTasa: number;
  participacion: number;
}

export interface DashboardCdatGrupo {
  concepto: string;
  cantidad: number;
  valorTotal: number;
  participacion: number;
}

export interface DashboardCdatTendencia {
  periodo: string;
  aperturas: number;
  cancelaciones: number;
  renovaciones: number;
  captacionNeta: number;
}

export interface DashboardCdatResponse {
  resumen: DashboardCdatResumen;
  agencias: DashboardCdatAgencia[];
  plazos: DashboardCdatGrupo[];
  tasas: DashboardCdatGrupo[];
  tendencia: DashboardCdatTendencia[];
  vencimientos: DashboardCdatVencimiento[];
}

export interface DashboardCdatVencimiento {

  rango: string;

  cantidad: number;

  valorTotal: number;

  participacion: number;

}

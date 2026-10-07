export interface DashboardCdatResumen {
  totalCdats: number;
  valorTotalCaptado: number;
  promedioTasa: number;
  promedioPlazo: number;
  vencen30Dias: number;
  totalAsociados: number;
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

export interface DashboardCdatVencimiento {
  rango: string;
  cantidad: number;
  valorTotal: number;
  participacion: number;
}

export interface DashboardCdatVencimientoDetalle {
  idCuentaCdat: number;
  codigoCdat: string;
  agencia: string;

  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;

  telefono: string;
  celularUno: string;
  celularDos: string;
  correoPersonal: string;

  fechaApertura: string | null;
  fechaVencimiento: string | null;
  diasParaVencer: number;
  plazoMeses: number;
  tasaNominalAnual: number;
  saldoActual: number;
}

export interface DashboardCdatTendencia {
  fechaCorte: string | null;
  periodo: string;
  cantidadCdats: number;
  valorCaptado: number;
}

export interface DashboardCdatResponse {
  resumen: DashboardCdatResumen;
  agencias: DashboardCdatAgencia[];
  plazos: DashboardCdatGrupo[];
  tasas: DashboardCdatGrupo[];
  vencimientos: DashboardCdatVencimiento[];
  tendencia: DashboardCdatTendencia[];
}

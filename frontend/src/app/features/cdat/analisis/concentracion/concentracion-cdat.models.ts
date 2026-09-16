export interface ConcentracionCdatResumen {
  fechaCorte: string;

  totalCdats: number;
  totalDepositantes: number;

  saldoTotal: number;
  saldoPromedio: number;

  tasaPonderada: number;
  plazoPonderadoMeses: number;

  participacionTop10: number;
  participacionTop20: number;
  participacionTop50: number;

  hhi: number;

  depositantesConcentran50: number;
  depositantesConcentran80: number;
}


export interface ConcentracionCdatDepositante {
  posicion: number;

  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;

  cantidadCdats: number;

  saldoTotal: number;

  participacion: number;
  participacionAcumulada: number;

  tasaPonderada: number;
  plazoPonderadoMeses: number;
}


export interface ConcentracionCdatDetalle {
  idCuentaCdat: number;
  codigoCdat: string;

  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  fechaApertura: string;
  fechaVencimiento: string;

  plazoMeses: number | null;
  plazoDias: number | null;

  tasaNominalAnual: number;

  valorApertura: number;
  saldoActual: number;

  participacionDepositante: number;
}


export interface ConcentracionCdatFiltros {
  fechaCorte: string;
  idAgencia: number | null;
}


export interface AgenciaConcentracionCdat {
  idAgencia: number;
  codigoAgencia?: string;
  nombreAgencia: string;
}

export interface TasasCondicionesCdatCorte {
  fechaCorte: string;
  anio: number;
  mes: number;
  cantidadAgencias: number;
  cantidadCdats: number;
}


export interface TasasCondicionesCdatResumen {
  cantidadCdats: number;
  cantidadDepositantes: number;

  saldoTotal: number;
  saldoPromedio: number;

  tasaNominalPonderada: number;
  tasaEfectivaPonderada: number;

  plazoPonderadoMeses: number;
}


export interface TasasCondicionesCdatCondicion {
  plazoMeses: number;

  amortizacionDeposito: string;
  nombreAmortizacion: string;

  cantidadCdats: number;

  saldoTotal: number;
  participacionSaldo: number;

  tasaNominalPonderada: number;
  tasaEfectivaPonderada: number;
}


export interface TasasCondicionesCdatRangoSaldo {
  rangoSaldo: string;

  cantidadCdats: number;

  saldoTotal: number;
  participacionSaldo: number;

  tasaNominalPonderada: number;
  tasaEfectivaPonderada: number;

  tasaEfectivaMinima: number;
  tasaEfectivaMaxima: number;
}


export interface TasasCondicionesCdatDetalle {
  idCuentaCdat: number;
  codigoCdat: string;

  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;

  fechaAperturaCdat: string;
  fechaVencimientoCdat: string;

  plazoMeses: number;
  plazoDias: number;

  amortizacionDeposito: string;
  nombreAmortizacion: string;

  valorAperturaCdat: number;
  saldoActualCdat: number;

  tasaNominalAnual: number;
  tasaEfectivaAnual: number;
}


export interface TasasCondicionesCdatFiltros {
  fechaCorte: string;
  idAgencia: number;
  plazoMeses: number;
  amortizacion: string;
  rangoSaldo: string;
}


export interface TasasCondicionesCdatAmortizacion {
  codigo: string;
  nombre: string;
}


export interface TasasCondicionesCdatRango {
  codigo: string;
  nombre: string;
}

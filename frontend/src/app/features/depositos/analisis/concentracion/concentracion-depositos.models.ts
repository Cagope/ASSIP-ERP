export interface ConcentracionDepositosRequest {
  fechaCorte: string;
  idAgencia: number | null;
  idFormaAhorro: number | null;
}

export interface ConcentracionDepositosResumen {
  fechaCorte: string;

  cantidadCuentas: number;
  cantidadAsociados: number;

  saldoCaptaciones: number;
  saldoAportes: number;
  saldoTotal: number;

  saldoPromedioPorCuenta: number;
  saldoPromedioPorAsociado: number;
  mayorSaldoAsociado: number;
}

export interface ConcentracionDepositosAgencia {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  cantidadCuentas: number;
  cantidadAsociados: number;

  saldo: number;
  porcentajeParticipacion: number;
}

export interface ConcentracionDepositosForma {
  idFormaAhorro: number;
  codigoForma: string;
  nombreForma: string;
  tipoCaptacionForma: string;

  cantidadCuentas: number;
  cantidadAsociados: number;

  saldo: number;
  porcentajeParticipacion: number;
}

export interface ConcentracionDepositosTop {
  grupo: string;
  cantidadAsociados: number;
  saldo: number;
  porcentajeParticipacion: number;
}

export interface ConcentracionDepositosAsociado {
  posicion: number;

  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;

  cantidadCuentas: number;

  saldo: number;
  porcentajeParticipacion: number;
  porcentajeAcumulado: number;
}

export interface ConcentracionDepositosResponse {
  resumen: ConcentracionDepositosResumen;
  agencias: ConcentracionDepositosAgencia[];
  formas: ConcentracionDepositosForma[];
  concentracion: ConcentracionDepositosTop[];
  asociados: ConcentracionDepositosAsociado[];
}

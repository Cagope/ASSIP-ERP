export interface CajaCapturaDepositosCheque {
  codigoBanco: string;
  numeroCheque: string;
  valorCheque: number;
}

export interface CajaCapturaDepositosRequest {
  idCaja: number;
  idAgencia: number;
  fechaContable: string;

  idCuentaAhorro: number;

  codigoOperacion: string;
  tipoMovimiento: string;

  tipoComprobante?: string | null;
  numeroComprobante: string;

  valorEfectivo: number;
  valorCheques: number;
  valorTransferencias: number;

  concepto?: string;
  observacion?: string;

  cheques: CajaCapturaDepositosCheque[];
}

export interface CajaCapturaDepositosCuenta {
  idCuentaAhorro: number;
  idAgencia: number;
  codigoAgencia?: string;
  nombreAgencia?: string;

  codigoCuenta: string;

  idDatosPersonal: number;
  tipoDocumento?: string;
  documento: string;
  nombreAsociado: string;

  idFormaAhorro: number;
  codigoForma: string;
  nombreForma: string;

  saldoActual: number;
  valorCanje: number;
  saldoDisponible: number;

  estadoCuenta: string;
  descripcionEstadoCuenta?: string;
  estadoOperativo: boolean;
  mensajeOperativo: string;

  tipoDocumentoSoporte?: string;
  numeroInicialLibreta?: string;
  numeroFinalLibreta?: string;

  gmfCuenta?: string;

  cuentaConjuntaReal?: boolean;
  conjuntos?: string;

  documentoPoder?: string;
  nombrePoder?: string;
  telefonoPoder?: string;
  celularPoder?: string;

  fechaAperturaCuenta?: string;

  firma1?: string;
  firma2?: string;
}

export interface SarlaftAlertaResultado {
  nombreRegla?: string;
  descripcion?: string;
  accionRecomendada?: string;
}

export interface SarlaftEvaluacionResultado {
  alerta: boolean;
  severidad?: string;
  descripcion?: string;
  idAlerta?: number;
  nombreRegla?: string;
  bloqueaOperacion?: boolean;
  accionRecomendada?: string;
  alertas?: SarlaftAlertaResultado[];
}

export interface CajaCapturaDepositosPreview {
  idCaja: number;
  idAgencia: number;
  fechaContable: string;

  idProvision: number;
  idCuentaAhorro: number;
  codigoCuenta: string;

  documento: string;
  nombreAsociado: string;

  codigoForma: string;
  nombreForma: string;

  codigoOperacion: string;
  nombreOperacion: string;
  naturaleza: string;

  valorEfectivo: number;
  valorCheques: number;
  valorTransferencias: number;
  valorTotal: number;

  saldoAnterior: number;
  valorCanje: number;
  saldoDisponible: number;
  saldoFinal: number;

  permiteAplicar: boolean;
  mensaje: string;

  movimientoInteragencia?: boolean;
  mensajeInteragencia?: string;

  sarlaft?: SarlaftEvaluacionResultado;
  errores?: string[];
}

export interface CajaCapturaDepositosResponse {
  idCuentaAhorro: number;
  codigoCuenta: string;

  documento: string;
  nombreAsociado: string;

  fechaContable: string;

  valorTotal: number;
  saldoAnterior: number;
  saldoFinal: number;

  movimientosCaja: number[];

  mensaje: string;

  requiereFormatoLavadoActivos?: boolean;
  idFormatoLavadoActivos?: number;
  mensajeLavadoActivos?: string;

}

export interface TipoMovimientoDeposito {
  codigoMovimiento: string;
  descripcion: string;
  accionMovimiento: string;
  contabilizacionDiaria?: boolean;
  generaGmf?: boolean;
  permiteInclusionManual?: boolean;
}

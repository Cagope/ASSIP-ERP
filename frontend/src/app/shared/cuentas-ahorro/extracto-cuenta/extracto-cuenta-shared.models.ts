export interface ExtractoCuentaSharedRequest {

  idCuentaAhorro: number;

  fechaInicial: string;

  fechaFinal: string;
}

export interface ExtractoCuentaSharedResumen {

  idCuentaAhorro: number;

  codigoCuenta: string;

  documento: string;

  nombreCompleto: string;

  direccion?: string;

  codigoForma: string;

  nombreForma: string;

  nombreAgencia: string;

  saldoInicial: number;

  totalCreditos: number;

  totalDebitos: number;

  saldoFinal: number;
}

export interface ExtractoCuentaSharedEstadistica {

  cantidadMovimientos: number;

  promedioCreditos: number;

  promedioDebitos: number;

  creditoMaximo: number;

  debitoMaximo: number;

  creditoMinimo: number;

  debitoMinimo: number;

  mediaMovimientos: number;

  medianaMovimientos: number;

  valorMovilizado: number;
}

export interface ExtractoCuentaSharedMovimiento {

  fechaMovimiento: string;

  horaMovimiento?: string;

  tipoMovimiento: string;

  descripcionMovimiento?: string;

  tipoComprobante?: string;

  numeroComprobante?: string;

  debito: number;

  credito: number;

  saldo: number;
}

export interface ExtractoCuentaSharedResponse {

  resumen: ExtractoCuentaSharedResumen | null;

  estadisticas: ExtractoCuentaSharedEstadistica | null;

  movimientos: ExtractoCuentaSharedMovimiento[];
}


export interface TransferenciaCuenta {

  // =====================================================
  // IDENTIFICACIÓN
  // =====================================================

  idCuentaAhorro: number;
  idAgencia: number;
  idDatosPersonal: number;

  // =====================================================
  // AGENCIA
  // =====================================================

  codigoAgencia: string;
  nombreAgencia: string;

  // =====================================================
  // TITULAR
  // =====================================================

  documento: string;
  nombreCompleto: string;

  // =====================================================
  // CUENTA Y PRODUCTO
  // =====================================================

  codigoCuenta: string;
  nombreForma: string;
  tipoCaptacion: string;

  // =====================================================
  // ESTADO
  // =====================================================

  estadoCuenta: string;
  descripcionEstadoCuenta: string;
  operativa: boolean;

  // =====================================================
  // INFORMACIÓN FINANCIERA
  // =====================================================

  saldoActual: number;
  saldoDisponible: number;
  permiteDebito: boolean;
}


// =========================================================
// ENTRADA DE TRANSFERENCIA
// =========================================================

export interface TransferenciaEntrada {
  idCuentaOrigen: number;
  idCuentaDestino: number;
  valor: number;
  fechaProceso: string;
  concepto: string;
}


// =========================================================
// MOVIMIENTO CONTABLE
// =========================================================

export interface TransferenciaMovimiento {
  codigoCuenta: string;
  nombreCuenta: string;
  detalle: string;
  valorDebito: number;
  valorCredito: number;
}


// =========================================================
// COMPROBANTE CONTABLE
// =========================================================

export interface TransferenciaComprobante {
  idAgencia: number;
  nombreAgencia: string;
  tipoComprobante: string;
  concepto: string;
  movimientos: TransferenciaMovimiento[];
  totalDebito: number;
  totalCredito: number;
}


// =========================================================
// VISTA PREVIA
// =========================================================

export interface TransferenciaPreview {
  origen: TransferenciaCuenta;
  destino: TransferenciaCuenta;
  valor: number;
  concepto: string;
  saldoOrigenFinal: number;
  saldoDestinoFinal: number;
  comprobantes: TransferenciaComprobante[];
}


// =========================================================
// RESULTADO DE TRANSFERENCIA
// =========================================================

export interface TransferenciaResultado {
  idCuentaOrigen: number;
  idCuentaDestino: number;
  valor: number;
  saldoOrigenFinal: number;
  saldoDestinoFinal: number;
  comprobantes: {
    idAgencia: number;
    tipoComprobante: string;
    numeroComprobante: string;
  }[];
}


// =========================================================
// FILTROS DE BÚSQUEDA
// =========================================================

export interface TransferenciaFiltro {
  documento: string;
  nombres: string;
  primerApellido: string;
  segundoApellido: string;
}

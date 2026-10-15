
export interface CajasSalidaChequesRequest {
  idCajaOrigen: number;
  fechaContable: string;
  numeroDocumento: string | null;
  observacion: string | null;
  idsCheques: number[];
}

export interface CajasSalidaChequesResponse {
  idCajaOrigen: number;
  idCajaControl: number;
  fechaContable: string;
  cantidadCheques: number;
  valorTotal: number;
  tipoComprobante: string;
  numeroComprobante: string;
  idsMovimientosCaja: number[];
  mensaje: string;
}

export interface CajasSalidaChequesCheque {
  idChequeRecibido: number;
  idCaja: number;
  codigoBanco: number;
  numeroCheque: string;
  valorCheque: number;
}

// =========================================================
// MOVIMIENTOS DE LA VISTA PREVIA CONTABLE
// =========================================================

export interface CajasSalidaChequesMovimientoPreview {
  idCatalogoCuenta: number;
  codigoCuenta: string;
  nombreCuenta: string;

  idAgencia: number;
  fechaAuxiliar: string;

  detalleMovimiento: string;

  valorDebito: number;
  valorCredito: number;
}

// =========================================================
// VISTA PREVIA DEL COMPROBANTE RC
// =========================================================

export interface CajasSalidaChequesPreview {
  idAgencia: number;

  idCajaOrigen: number;
  idCajaControl: number;

  fechaContable: string;

  cantidadCheques: number;
  valorTotal: number;

  tipoComprobante: string;
  conceptoComprobante: string;

  movimientos: CajasSalidaChequesMovimientoPreview[];

  totalDebito: number;
  totalCredito: number;
  diferencia: number;
  cuadrado: boolean;
}

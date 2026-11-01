/** Contrato de vista previa de importación del backend. */
export interface ComprobanteImportacionMovimiento {
  numeroFila: number;
  codigoCuenta: string;
  idCatalogoCuenta: number | null;
  documentoTercero: string;
  idDatosPersonal: number | null;
  detalleMovimiento: string;
  valorDebito: number | null;
  valorCredito: number | null;
  valorBase: number | null;
  idTiposControl: number | null;
  documentoSoporte: string | null;
}

export interface ComprobanteImportacionError {
  numeroFila: number;
  columna: string;
  mensaje: string;
}

export interface ComprobanteImportacionResponse {
  nombreArchivo: string;
  idAgencia: number;
  totalFilas: number;
  filasValidas: number;
  filasConErrores: number;
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
  puedeGrabar: boolean;
  movimientos: ComprobanteImportacionMovimiento[];
  errores: ComprobanteImportacionError[];
}

export interface ComprobanteImportacionDatos {
  idAgencia: number;
  tipoComprobante: string;
  fechaContable: string;
  conceptoComprobante: string;
  archivo: File;
}

export interface ComprobanteImportacionAgencia {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

export interface ComprobanteImportacionTipo {
  tipoComprobante: string;
  nombreTipoComprobante: string;
  comprobanteActivo: boolean;
}

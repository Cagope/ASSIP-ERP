
/** Filtros de consulta del Resumen diario detallado. */
export interface ResumenDiarioDetalladoFiltro {
  idAgencia: number;
  fechaInicial: string;
  fechaFinal: string;
}

/** Movimiento individual de un auxiliar contable activo. */
export interface ResumenDiarioDetalladoFila {
  idAuxiliarContable: number;
  fecha: string;
  tipoComprobante: string;
  numeroComprobante: string;
  codigoCuenta: string;
  nombreCuenta: string;
  documentoTercero: string | null;
  nombreTercero: string | null;
  detalleMovimiento: string | null;
  valorDebito: number;
  valorCredito: number;
  origenModulo: string | null;
  documentoOrigen: string | null;
  documentoSoporte: string | null;
}

/** Movimientos y subtotales correspondientes a una fecha. */
export interface ResumenDiarioDetalladoDia {
  fecha: string;
  movimientos: ResumenDiarioDetalladoFila[];
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
}

/** Resultado completo del informe, con totales generales. */
export interface ResumenDiarioDetalladoResponse {
  idAgencia: number;
  fechaInicial: string;
  fechaFinal: string;
  dias: ResumenDiarioDetalladoDia[];
  cantidadMovimientos: number;
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
}

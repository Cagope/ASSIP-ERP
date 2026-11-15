
/**
 * Contratos del proceso de Costas Judiciales.
 * Los identificadores se obtienen mediante selectores y búsquedas.
 * Nunca se solicitan manualmente al funcionario.
 */

/** Solicitud de previsualización y confirmación. */
export interface CostasJudicialesRequest {
  idCarteraCredito: number;
  fechaMovimiento: string;
  tipoComprobante: string;
  idDatosPersonalTercero: number | null;
  valor: number;
  observacion: string;
}

/** Movimiento contable presentado en la vista previa. */
export interface CostasJudicialesLinea {
  codigoCuenta: string;
  nombreCuenta: string;
  documentoTercero: string;
  idCatalogoCuenta: number;
  idDatosPersonal: number | null;
  debito: number;
  credito: number;
}

/** Respuesta de previsualización del comprobante. */
export interface CostasJudicialesVistaPrevia {
  idCredito: number;
  idAgencia: number;
  tipoComprobante: string;
  numeroSugerido: string;
  fecha: string;
  valor: number;
  modalidad: 'COSTAS_JUDICIALES_CXP' | 'COSTAS_JUDICIALES_AGENCIA';
  movimientos: CostasJudicialesLinea[];
}

/** Resultado de la contabilización definitiva. */
export interface CostasJudicialesResultado {
  idDetalle: number;
  tipoComprobante: string;
  numeroComprobante: string;
  saldoCostasJudiciales: number;
}

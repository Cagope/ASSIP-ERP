
/**
 * Filtros para consultar el informe Secuencial de comprobantes.
 * La agencia se selecciona entre las autorizadas para el usuario.
 */
export interface SecuencialComprobantesFiltro {
  idAgencia: number;
  fechaInicial: string;
  fechaFinal: string;
}

/**
 * Representa un comprobante contable consolidado.
 * Los movimientos se agrupan por tipo, número y fecha.
 */
export interface SecuencialComprobantesFila {
  tipoComprobante: string;
  nombreTipoComprobante: string;
  numeroComprobante: string;
  fecha: string;
  totalDebitos: number;
  totalCreditos: number;
}

/**
 * Resultado de la consulta del Secuencial de comprobantes.
 * Incluye los comprobantes y los totales generales del período.
 */
export interface SecuencialComprobantesResponse {
  idAgencia: number;
  fechaInicial: string;
  fechaFinal: string;
  filas: SecuencialComprobantesFila[];
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
}

/**
 * Agrupación visual de comprobantes por tipo.
 * Permite presentar encabezados, detalle y subtotales por corte.
 */
export interface SecuencialComprobantesGrupo {
  tipoComprobante: string;
  nombreTipoComprobante: string;
  comprobantes: SecuencialComprobantesFila[];
  cantidadComprobantes: number;
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
}

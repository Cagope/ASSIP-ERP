
/**
 * Modelos de consulta y edición de comprobantes contables manuales.
 * Conserva los identificadores internos para garantizar la edición.
 */
// =========================================================
// RESUMEN DE COMPROBANTES
// =========================================================
/** Información presentada en los resultados de búsqueda. */
export interface ResumenComprobante {
  // Agencia
  idAgencia: number;
  codigoAgencia: string | null;
  nombreAgencia: string | null;
  // Tipo y número
  tipoComprobante: string;
  nombreTipoComprobante: string | null;
  numeroComprobante: string;
  // Información general
  fechaContable: string;
  conceptoComprobante: string;
  estadoOrigen: string;
  // Totales
  totalDebito: number;
  totalCredito: number;
  cantidadMovimientos: number;
}
// =========================================================
// CABECERA DEL COMPROBANTE
// =========================================================
/** Información general del comprobante seleccionado. */
export interface CabeceraComprobante {
  // Agencia
  idAgencia: number;
  codigoAgencia: string | null;
  nombreAgencia: string | null;
  // Identificación
  tipoComprobante: string;
  nombreTipoComprobante: string | null;
  numeroComprobante: string;
  // Concepto
  conceptoComprobante: string;
  // Origen
  origenTipo: string;
  moduloOrigen: string;
  procesoOrigen: string | null;
  estadoOrigen: string;
  // Fecha
  fechaContable: string;
}
// =========================================================
// MOVIMIENTOS CONTABLES
// =========================================================
/**
 * Movimiento contable con información descriptiva.
 * Conserva los identificadores de cuentas, terceros y controles.
 */
export interface MovimientoComprobante {
  // Identificación
  idAuxiliarContable: number;
  // Cuenta contable
  idCatalogoCuenta: number;
  codigoCuenta: string | null;
  nombreCuenta: string | null;
  // Tercero
  idDatosPersonal: number | null;
  documentoTercero: string | null;
  nombreTercero: string | null;
  // Fecha y descripción
  fechaAuxiliar: string;
  detalleMovimiento: string;
  estadoMovimiento: string;
  // Valores contables
  valorDebito: number | null;
  valorCredito: number | null;
  valorBase: number | null;
  // Control documental
  idControlesAuxiliares: number | null;
  idTiposControl: number | null;
  numeroDocumento: string | null;
}
// =========================================================
// COMPROBANTE COMPLETO
// =========================================================
/** Respuesta completa de la consulta de un comprobante. */
export interface ComprobanteCompleto {
  cabecera: CabeceraComprobante;
  movimientos: MovimientoComprobante[];
}
// =========================================================
// FILTROS DE CONSULTA
// =========================================================
/** Filtros opcionales utilizados para buscar comprobantes. */
export interface FiltrosComprobantes {
  idAgencia?: number;
  tipoComprobante?: string;
  numeroComprobante?: string;
  fechaDesde?: string;
  fechaHasta?: string;
  concepto?: string;
  idDatosPersonal?: number;
}
// =========================================================
// TIPOS DE CONTROL DOCUMENTAL
// =========================================================
/** Tipo de control documental asociado a un movimiento. */
export interface TipoControl {
  idTiposControl: number;
  nombreTipoControl: string;
  pideDocumento: boolean;
}
// =========================================================
// MOVIMIENTO PARA EDICIÓN
// =========================================================
/** Información enviada al backend para actualizar un movimiento. */
export interface MovimientoEdicion {
  idAuxiliarContable: number;
  idCatalogoCuenta: number;
  idDatosPersonal: number;
  detalleMovimiento: string;
  valorDebito: number;
  valorCredito: number;
  valorBase: number;
  idTiposControl: number | null;
  numeroDocumento: string | null;
}
// =========================================================
// SOLICITUD DE EDICIÓN
// =========================================================
/** Solicitud de actualización de un comprobante manual. */
export interface SolicitudEdicion {
  conceptoComprobante: string;
  movimientos: MovimientoEdicion[];
}
// =========================================================
// RESPUESTA DE EDICIÓN
// =========================================================
/** Confirmación de actualización del comprobante. */
export interface ComprobanteEditadoResponse {
  idAgencia: number;
  tipoComprobante: string;
  numeroComprobante: string;
  mensaje: string;
}

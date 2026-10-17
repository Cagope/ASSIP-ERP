/**
 * Modelos para la autorización de comprobantes de Tesorería.
 *
 * Correspondencia con los DTO del backend:
 * - ComprobanteResumenDTO
 * - ComprobanteDetalleDTO
 * - ComprobantePartidaDTO
 * - ComprobanteMovimientoCajaDTO
 * - ComprobanteDecisionRequestDTO
 * - ComprobanteDecisionResponseDTO
 *
 * Los usuarios, fechas y estados definitivos son
 * determinados por el backend.
 */

/**
 * Estados de los comprobantes de Tesorería.
 */
export type EstadoComprobante =
  | 'PENDIENTE'
  | 'CONTABILIZADO'
  | 'RECHAZADO'
  | 'ANULADO';

/**
 * Decisiones permitidas por el backend.
 */
export type DecisionComprobante =
  | 'AUTORIZAR'
  | 'RECHAZAR'
  | 'ANULAR';

/**
 * Resumen de un comprobante.
 *
 * Utilizado en la bandeja de pendientes.
 */
export interface ComprobanteResumen {
  idComprobante: number;
  idAgencia: number;

  tipoComprobante: string;
  numeroComprobante: string | null;
  procesoOrigen: string;

  fechaDocumento: string;
  fechaContable: string | null;

  concepto: string;
  documentoSoporte: string | null;

  totalDebito: number;
  totalCredito: number;

  estado: string;

  idUsuarioElaboracion: number;
  fechaElaboracion: string;

  idUsuarioAutorizacion: number | null;
  fechaAutorizacion: string | null;
}

/**
 * Partida contable preparada.
 *
 * Mientras el comprobante permanezca pendiente,
 * no representa un movimiento contable definitivo.
 */
export interface ComprobantePartida {
  idComprobanteDetalle: number;
  idComprobante: number;
  numeroLinea: number;

  idCatalogoCuenta: number;
  idAgencia: number;
  idDatosPersonal: number | null;

  detalleMovimiento: string;

  valorDebito: number;
  valorCredito: number;
  valorBase: number;

  origenModulo: string | null;
  documentoOrigen: string | null;
  documentoSoporte: string | null;
}

/**
 * Movimiento de caja proyectado.
 *
 * No afecta el efectivo hasta que el backend
 * autorice y aplique el comprobante.
 */
export interface ComprobanteMovimientoCaja {
  idComprobanteMovimientoCaja: number;
  idComprobante: number;
  numeroLinea: number;

  idCaja: number;
  idDepartamentoOperativo: number;
  idOperacionCaja: number;

  moduloOrigen: string;
  procesoOrigen: string;

  tipoDocumento: string;
  numeroDocumento: string;
  codigoReferencia: string;

  naturaleza: string;
  medioPago: string;

  valor: number;
  concepto: string;
}

/**
 * Información completa de un comprobante.
 *
 * Incluye cabecera, auditoría, partidas contables
 * y movimientos de caja preparados.
 */
export interface ComprobanteDetalle
  extends ComprobanteResumen {

  idUsuarioRechazo: number | null;
  fechaRechazo: string | null;
  motivoRechazo: string | null;

  idUsuarioAnulacion: number | null;
  fechaAnulacion: string | null;
  motivoAnulacion: string | null;

  partidasContables: ComprobantePartida[];
  movimientosCaja: ComprobanteMovimientoCaja[];
}

/**
 * Solicitud de decisión sobre un comprobante.
 *
 * RECHAZAR y ANULAR requieren motivo.
 * El backend identifica al usuario autenticado.
 */
export interface ComprobanteDecisionRequest {
  decision: DecisionComprobante;
  motivo: string | null;
}

/**
 * Respuesta del backend después de procesar
 * una autorización, rechazo o anulación.
 */
export interface ComprobanteDecisionResponse {
  idComprobante: number;
  idAgencia: number;

  tipoComprobante: string;
  numeroComprobante: string | null;
  procesoOrigen: string;

  estado: string;
  decision: DecisionComprobante;
  mensaje: string;

  fechaDocumento: string;
  fechaContable: string | null;
  fechaDecision: string;

  idUsuarioDecision: number;
  motivo: string | null;
}

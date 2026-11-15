import type {
  PagoCuotasCreditoResumen,
  PagoCuotasCreditoDetalle,
  PagoCuotasCajaRecaudo,
  PagoCuotasCuentaAhorro
} from '../pago-cuotas_v1/pago-cuotas_v1.models';

/** Contratos reutilizados de Pago de Cuotas para consultas y medios disponibles. */
export type ProrrogasCreditoResumen = PagoCuotasCreditoResumen;
export type ProrrogasCreditoDetalle = PagoCuotasCreditoDetalle;
export type ProrrogasCaja = PagoCuotasCajaRecaudo;
export type ProrrogasCuentaAhorro = PagoCuotasCuentaAhorro;

export type ProrrogasTipoMedio = 'CAJA' | 'AHORROS' | 'CXP';

/** Coincide con PagoCuotasMedioRecaudoDTO del backend. */
export interface ProrrogasMedioRecaudo {
  tipoMedio: ProrrogasTipoMedio;
  valor: number;
  idCaja: number | null;
  idCuentaAhorro: number | null;
  idCuentaPorPagar: number | null;
}

/** Solicitud de prórroga con los seis conceptos financieros. */
export interface ProrrogasSolicitud {
  idCarteraCredito: number;
  mesesProrroga: 1 | 2 | 3;
  valorInteresCorriente: number;
  valorInteresMora: number;
  valorCapital: number;
  valorSeguro: number;
  valorAlivios: number;
  valorCostasJudiciales: number;
  mediosRecaudo: ProrrogasMedioRecaudo[];
  observacion: string;
}

/** Vista previa de prórroga calculada por el backend. */
export interface ProrrogasPreview {
  idCarteraCredito: number;
  idAgencia: number;
  numeroPagare: string;
  documento: string;
  nombreCompleto: string;
  estadoCredito: string;
  saldoCapital: number;
  valorCuota: number;
  numeroCuota: number;
  diasMora: number;
  fechaConsulta: string;
  proximaFechaCapital: string | null;
  proximaFechaInteres: string | null;
  fechaVencimiento: string | null;
  mesesProrroga: number;
  proximaFechaCapitalNueva: string | null;
  proximaFechaInteresNueva: string | null;
  fechaVencimientoNueva: string | null;
  valorInteresCorriente: number;
  valorInteresMora: number;
  valorCapital: number;
  valorSeguro: number;
  valorAlivios: number;
  valorCostasJudiciales: number;
  valorTotalRecaudo: number;
  tieneProrrogaActiva: boolean;
  permiteProrroga: boolean;
  mensajeValidacion: string | null;
}

/** Resultado de la aplicación financiera de la prórroga. */
export interface ProrrogasResultado {
  idCreditoProrroga: number;
  idCarteraCredito: number;
  numeroProrroga: number;
  mesesProrroga: number;
  diasProrroga: number;
  fechaProrroga: string;
  proximaFechaCapitalAnterior: string | null;
  proximaFechaCapitalNueva: string | null;
  proximaFechaInteresAnterior: string | null;
  proximaFechaInteresNueva: string | null;
  fechaVencimientoAnterior: string | null;
  fechaVencimientoNueva: string | null;
  valorCapital: number;
  valorInteresCorriente: number;
  valorInteresMora: number;
  valorSeguro: number;
  valorAlivios: number;
  valorCostasJudiciales: number;
  valorTotalPagado: number;
  tipoComprobante: string;
  numeroComprobante: string;
  fechaComprobante: string;
  estadoProrroga: string;
  mensaje: string;
}

/** Historial contractual y financiero de prórrogas. */
export interface ProrrogasHistorial {
  idCreditoProrroga: number;
  idCarteraCredito: number;
  numeroProrroga: number;
  fechaProrroga: string;
  diasProrroga: number | null;
  mesesProrroga: number;
  proximaFechaCapitalAnterior: string | null;
  proximaFechaInteresAnterior: string | null;
  fechaVencimientoAnterior: string | null;
  plazoAnterior: number | null;
  proximaFechaCapitalNueva: string | null;
  proximaFechaInteresNueva: string | null;
  fechaVencimientoNueva: string | null;
  plazoNuevo: number | null;
  valorCapital: number;
  valorInteresCorriente: number;
  valorInteresMora: number;
  valorSeguro: number;
  valorAlivios: number;
  valorCostasJudiciales: number;
  valorTotalPagado: number;
  tipoComprobante: string;
  numeroComprobante: string;
  fechaComprobante: string;
  idAgencia: number | null;
  estadoProrroga: string;
  fechaAplicacion: string | null;
  observacion: string | null;
}

/** Movimientos del comprobante contable generado por el backend. */
export interface ProrrogasMovimientoContable {
  codigoCuenta: string;
  nombreCuenta: string;
  detalleMovimiento: string;
  valorDebito: number;
  valorCredito: number;
}

/** Liquidación de prórroga calculada exclusivamente por el backend. */
export interface ProrrogasLiquidacion {
  idCarteraCredito: number;
  fechaLiquidacion: string;
  periodoInteresMeses: number;
  tipoModalidadInteres: string;
  saldoCapital: number;
  valorInteresCorriente: number;
  valorInteresMora: number;
  valorCapital: number;
  valorSeguro: number;
  valorAlivios: number;
  valorCostasJudiciales: number;
  valorTotalSugerido: number;
}

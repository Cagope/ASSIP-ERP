
/**
 * ASSIP ERP
 * Tesorería - Operaciones Pendientes
 *
 * Modelos de obligaciones y distribución de pagos.
 * Una obligación admite múltiples ejecuciones,
 * cuentas bancarias, transferencias y cheques.
 */

// =====================================================
// ESTADOS Y TIPOS
// =====================================================

export type NaturalezaOperacion =
  | 'ENTRADA'
  | 'SALIDA';

export type EstadoOperacion =
  | 'PENDIENTE'
  | 'PARCIAL'
  | 'COMPLETADA'
  | 'ANULADA';

export type EstadoEjecucion =
  | 'PREPARADA'
  | 'CONFIRMADA'
  | 'RECHAZADA'
  | 'ANULADA';

export type MedioPagoOperacion =
  | 'TRANSFERENCIA'
  | 'CHEQUE';

// =====================================================
// OBLIGACIONES PENDIENTES
// Respuesta real del backend: snake_case
// =====================================================

export interface OperacionPendiente {
  id_operacion_pendiente: number;
  id_agencia: number;

  modulo_origen: string;
  proceso_origen: string;
  tabla_origen: string;
  id_origen: number;

  id_datos_personal: number | null;

  naturaleza: NaturalezaOperacion;
  fecha_documento: string;
  concepto: string;

  valor_original: number;
  valor_ejecutado: number;
  saldo_pendiente: number;

  estado: EstadoOperacion;
  fecha_creacion: string;
}

// =====================================================
// EJECUCIONES REGISTRADAS
// =====================================================

export interface OperacionEjecucion {
  id_operacion_ejecucion: number;
  id_operacion_pendiente: number;
  numero_linea: number;

  id_beneficiario: number | null;
  id_cuenta_bancaria: number | null;

  medio_pago: string;
  numero_documento: string | null;

  valor: number;
  estado: EstadoEjecucion;

  fecha_ejecucion: string | null;
  tipo_comprobante: string | null;
  numero_comprobante: number | null;

  observaciones: string | null;
}

// =====================================================
// SALDO DISPONIBLE
// Respuesta real del endpoint /saldo
// =====================================================

export interface OperacionSaldo {
  idOperacionPendiente: number;
  saldoDisponible: number;
}

// =====================================================
// DISTRIBUCIÓN DE PAGOS EN PANTALLA
// Modelo local; todavía no es un contrato de API.
// =====================================================

export interface LineaDistribucionPago {
  idTemporal: number;

  medioPago: MedioPagoOperacion;

  idBeneficiario: number | null;
  idCuentaBancaria: number | null;

  numeroDocumento: string;
  valor: number;

  observaciones: string;
}

export interface ResumenDistribucionPago {
  valorObligacion: number;
  valorEjecutado: number;
  saldoDisponible: number;

  totalDistribuido: number;
  saldoSinDistribuir: number;

  cantidadLineas: number;
  distribucionValida: boolean;
}


export interface CuentaBancariaParaPago {
  id_cuenta_bancaria: number;
  id_agencia: number;
  nombre_banco: string;
  numero_cuenta: string;
  nombre_cuenta: string;
  id_cuenta_contable: number;
  codigo_cuenta: string;
  nombre_cuenta_contable: string;
}

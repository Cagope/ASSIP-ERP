
//
// =========================================================
// MODELOS
// TRASLADO INTEGRAL DE CUENTAS ENTRE AGENCIAS
// =========================================================
//


// =========================================================
// ENTRADA DEL TRASLADO
// =========================================================

export interface TrasladoEntrada {

  // Identificación del asociado
  idDatosPersonal: number;

  // Agencias involucradas
  idAgenciaOrigen: number;
  idAgenciaDestino: number;

  // Fecha de ejecución
  fechaProceso: string;

  // Comentario o justificación
  comentario: string;

}


// =========================================================
// CUENTA DE AHORRO
// =========================================================

export interface TrasladoCuenta {

  // Identificación de la cuenta
  idCuentaAhorro: number;

  // Agencia y forma de ahorro
  idAgencia: number;
  idFormaAhorro: number;

  // Información de la cuenta
  codigoCuenta: string;
  codigoForma: string;
  nombreForma: string;
  estadoCuenta: string;

  // Información financiera
  saldoActual: number;

  // Validación del traslado
  trasladable: boolean;
  observacion: string;

}


// =========================================================
// VALIDACIONES DEL TRASLADO
// =========================================================

export interface TrasladoValidacion {

  // Resultado general
  permitido: boolean;

  // Restricciones por productos
  tieneCdatBloqueante: boolean;
  tieneCarteraBloqueante: boolean;

  // Cuentas existentes en destino
  tieneCuentasDuplicadasDestino: boolean;

  // Mensajes de validación
  mensajes: string[];

}


// =========================================================
// MOVIMIENTO CONTABLE PRELIMINAR
// =========================================================

export interface TrasladoMovimiento {

  // Identificación de la cuenta contable
  codigoCuenta: string;
  nombreCuenta: string;

  // Descripción del movimiento
  detalle: string;

  // Valores contables
  valorDebito: number;
  valorCredito: number;

}


// =========================================================
// COMPROBANTE CONTABLE PRELIMINAR
// =========================================================

export interface TrasladoComprobante {

  // Agencia responsable del comprobante
  idAgencia: number;
  nombreAgencia: string;

  // Identificación y concepto
  tipoComprobante: string;
  concepto: string;

  // Movimientos contables
  movimientos: TrasladoMovimiento[];

  // Totales del comprobante
  totalDebito: number;
  totalCredito: number;

}


// =========================================================
// VISTA PREVIA DEL TRASLADO
// =========================================================

export interface TrasladoPreview {

  // Identificación del asociado
  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;

  // Agencias involucradas
  idAgenciaOrigen: number;
  idAgenciaDestino: number;

  // Cuentas que serán trasladadas
  cuentas: TrasladoCuenta[];

  // Valor total del traslado
  totalTraslado: number;

  // Resultado de las validaciones
  validacion: TrasladoValidacion;

  // Comprobantes contables preliminares
  comprobantes: TrasladoComprobante[];

}


// =========================================================
// CUENTA TRASLADADA
// =========================================================

export interface TrasladoCuentaTrasladada {

  // Identificación de las cuentas
  idCuentaOrigen: number;
  idCuentaDestino: number;

  // Código de la cuenta de ahorro
  codigoCuenta: string;

  // Valor efectivamente trasladado
  valorTrasladado: number;

}


// =========================================================
// COMPROBANTE CONTABLE GENERADO
// =========================================================

export interface TrasladoComprobanteGenerado {

  // Agencia del comprobante
  idAgencia: number;

  // Identificación del comprobante
  tipoComprobante: string;
  numeroComprobante: string;

}


// =========================================================
// RESULTADO DEFINITIVO DEL TRASLADO
// =========================================================

export interface TrasladoResultado {

  // Identificación del asociado
  idDatosPersonal: number;

  // Agencias involucradas
  idAgenciaOrigen: number;
  idAgenciaDestino: number;

  // Resultado financiero
  totalTrasladado: number;
  cantidadCuentas: number;

  // Cuentas trasladadas
  cuentas: TrasladoCuentaTrasladada[];

  // Comprobantes contables definitivos
  comprobantes: TrasladoComprobanteGenerado[];

}

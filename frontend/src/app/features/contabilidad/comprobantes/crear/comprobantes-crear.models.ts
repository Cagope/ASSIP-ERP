
/**
 * Modelos para la creación de comprobantes contables manuales.
 *
 * Los importes monetarios se manejan como números sin
 * separadores de miles. El formato visual corresponde
 * al componente y a las directivas del ERP.
 */

// =========================================================
// SOLICITUD DE CREACIÓN
// =========================================================

/**
 * Movimiento contable enviado al backend.
 *
 * Cada movimiento debe tener una cuenta y un tercero.
 * La base es independiente de los valores débito y crédito.
 *
 * El tipo de control se informa únicamente cuando
 * la cuenta exige control de entrada o salida.
 */
export interface ComprobanteMovimientoCrear {
  idCatalogoCuenta: number;
  idDatosPersonal: number;
  detalleMovimiento: string;
  valorDebito: number;
  valorCredito: number;
  valorBase: number;
  idTiposControl: number | null;
  documentoSoporte: string | null;
}

/**
 * Solicitud de creación de un comprobante manual.
 *
 * El número definitivo lo asigna el backend.
 */
export interface ComprobanteCrearRequest {
  idAgencia: number;
  tipoComprobante: string;
  fechaContable: string;
  conceptoComprobante: string;
  movimientos: ComprobanteMovimientoCrear[];
}

/**
 * Respuesta del backend después de crear el comprobante.
 */
export interface ComprobanteCrearResponse {
  idAgencia: number;
  tipoComprobante: string;
  numeroComprobante: string;
  mensaje: string;
}

// =========================================================
// OPCIONES PARA SELECTORES
// =========================================================

/**
 * Agencia disponible para la operación contable.
 *
 * El código se presenta junto con el nombre.
 * El identificador interno no se digita manualmente.
 */
export interface ComprobanteAgenciaOpcion {
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
}

/**
 * Tipo de comprobante disponible para la agencia.
 */
export interface ComprobanteTipoOpcion {
  tipoComprobante: string;
  nombreTipo: string;
}

/**
 * Tipo de control documental configurado en PostgreSQL.
 *
 * pideDocumento determina si se exige una referencia
 * para registrar el movimiento.
 *
 * Los valores se deben obtener del backend y no
 * mantener como identificadores fijos en Angular.
 */
export interface ComprobanteTipoControlOpcion {
  idTiposControl: number;
  nombreTipoControl: string;
  pideDocumento: boolean;
}

// =========================================================
// BÚSQUEDA DE CUENTAS CONTABLES
// =========================================================

/**
 * Cuenta seleccionable en una línea del comprobante.
 *
 * La búsqueda debe limitarse a cuentas operables
 * pertenecientes a la agencia del comprobante.
 */
export interface ComprobanteCuentaOpcion {
  idCatalogoCuenta: number;
  idAgencia: number;
  codigoCuenta: string;
  nombreCuenta: string;
  operable: boolean;
  controlEntradaSalida: boolean;
}

// =========================================================
// BÚSQUEDA DE TERCEROS
// =========================================================

/**
 * Tercero seleccionable en cada movimiento.
 *
 * Permite buscar por documento, nombres, apellidos
 * o razón social, según corresponda.
 */
export interface ComprobanteTerceroOpcion {
  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;
  tipoPersona?: string;
}

// =========================================================
// CAPTURA DE MOVIMIENTOS
// =========================================================

/**
 * Fila editable del comprobante.
 *
 * Los identificadores se conservan por separado
 * de las descripciones visibles para el usuario.
 *
 * El tipo de control y su documento pertenecen
 * exclusivamente a esta línea contable.
 */
export interface ComprobanteMovimientoFormulario {
  idFila: number;

  idCatalogoCuenta: number | null;
  codigoCuenta: string;
  nombreCuenta: string;

  idDatosPersonal: number | null;
  documentoTercero: string;
  nombreTercero: string;

  detalleMovimiento: string;

  valorDebito: number;
  valorCredito: number;
  valorBase: number;

  idTiposControl: number | null;
  documentoSoporte: string;

  requiereControlEntradaSalida: boolean;
  requiereDocumentoSoporte: boolean;
}

/**
 * Totales calculados de los movimientos.
 *
 * La base no participa en el cuadre contable.
 */
export interface ComprobanteTotales {
  totalDebito: number;
  totalCredito: number;
  diferencia: number;
  cuadrado: boolean;
}

// =========================================================
// ESTADO DEL FORMULARIO
// =========================================================

/**
 * Datos generales y movimientos en edición.
 *
 * El consecutivo no se captura porque lo genera
 * el backend al confirmar la creación.
 */
export interface ComprobanteCrearFormulario {
  idAgencia: number | null;
  tipoComprobante: string;
  fechaContable: string;
  conceptoComprobante: string;
  movimientos: ComprobanteMovimientoFormulario[];
}

/**
 * Crea una fila vacía con valores monetarios iniciales.
 *
 * No asigna automáticamente un tipo de control:
 * este dependerá de la cuenta seleccionada.
 *
 * El tercero podrá proponerse desde la fila anterior
 * cuando implementemos la captura rápida.
 */
export function crearMovimientoVacio(
  idFila: number
): ComprobanteMovimientoFormulario {
  return {
    idFila,

    idCatalogoCuenta: null,
    codigoCuenta: '',
    nombreCuenta: '',

    idDatosPersonal: null,
    documentoTercero: '',
    nombreTercero: '',

    detalleMovimiento: '',

    valorDebito: 0,
    valorCredito: 0,
    valorBase: 0,

    idTiposControl: null,
    documentoSoporte: '',

    requiereControlEntradaSalida: false,
    requiereDocumentoSoporte: false
  };
}

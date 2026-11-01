
/**
 * Modalidades disponibles para consultar el Libro diario resumido.
 *
 * CUENTA: agrupa por fecha y cuenta contable.
 * TIPO_COMPROBANTE: agrupa por fecha y tipo de comprobante.
 */
export type LibroDiarioResumidoModalidad =
  | 'CUENTA'
  | 'TIPO_COMPROBANTE';

/**
 * Filtros enviados al backend para consultar el informe.
 *
 * La agencia se selecciona mediante una lista de agencias
 * autorizadas; el usuario no digita identificadores.
 *
 * Las fechas se envían en formato ISO: yyyy-MM-dd.
 */
export interface LibroDiarioResumidoFiltro {
  idAgencia: number;
  fechaInicial: string;
  fechaFinal: string;
  modalidad: LibroDiarioResumidoModalidad;
}

/**
 * Fila del Libro diario resumido.
 *
 * Representa los movimientos contables activos agrupados
 * por fecha y cuenta, o por fecha y tipo de comprobante.
 */
export interface LibroDiarioResumidoFila {
  fecha: string;
  codigoAgrupacion: string;
  nombreAgrupacion: string;
  totalDebitos: number;
  totalCreditos: number;
}

/**
 * Respuesta del backend para el Libro diario resumido.
 *
 * Incluye los filtros aplicados, las filas agrupadas
 * y los totales generales del informe.
 */
export interface LibroDiarioResumidoResponse {
  idAgencia: number;
  fechaInicial: string;
  fechaFinal: string;
  modalidad: LibroDiarioResumidoModalidad;
  filas: LibroDiarioResumidoFila[];
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
}

/**
 * Opciones de agrupación disponibles para el selector del informe.
 */
export const LIBRO_DIARIO_RESUMIDO_MODALIDADES: ReadonlyArray<{
  value: LibroDiarioResumidoModalidad;
  label: string;
}> = [
  {
    value: 'CUENTA',
    label: 'Por fecha y cuenta contable'
  },
  {
    value: 'TIPO_COMPROBANTE',
    label: 'Por fecha y tipo de comprobante'
  }
];

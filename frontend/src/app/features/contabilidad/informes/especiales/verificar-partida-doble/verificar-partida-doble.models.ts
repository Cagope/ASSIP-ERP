/** Parámetros de la verificación de partida doble. */
export interface VerificarPartidaDobleFiltro {
  idAgencia: number;
  fechaInicial: string;
  fechaFinal: string;
}

/** Comprobante consolidado con diferencia calculada en el backend. */
export interface VerificarPartidaDobleFila {
  tipoComprobante: string;
  nombreTipoComprobante: string;
  numeroComprobante: string;
  fecha: string;
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
  descuadrado: boolean;
}

/** Totales y comprobantes verificados de la agencia y período. */
export interface VerificarPartidaDobleResponse extends VerificarPartidaDobleFiltro {
  filas: VerificarPartidaDobleFila[];
  cantidadComprobantes: number;
  cantidadCuadrados: number;
  cantidadDescuadrados: number;
  totalDebitos: number;
  totalCreditos: number;
  diferencia: number;
}

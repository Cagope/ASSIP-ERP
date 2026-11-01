export interface ComprobanteResumenFiltro {
  idAgencia: number;
  fechaInicial: string;
  fechaFinal: string;
}

export interface ComprobanteResumenFila {
  codigoCuenta: string;
  nombreCuenta: string;
  totalDebitos: number;
  totalCreditos: number;
}

export interface ComprobanteResumenResponse extends ComprobanteResumenFiltro {
  filas: ComprobanteResumenFila[];
  cantidadCuentas: number;
  totalDebitos: number;
  totalCreditos: number;
}

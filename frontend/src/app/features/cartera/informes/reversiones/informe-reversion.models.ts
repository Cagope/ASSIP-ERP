/** Contrato de consulta del informe de reversiones de Cartera. */
export interface InformeReversionFiltro {
  fechaInicial: string;
  fechaFinal: string;
  idAgencia?: number | null;
  asociado?: string;
  pagare?: string;
  funcionario?: string;
}

export interface InformeReversion {
  idPago: number;
  idAgenciaCredito: number;
  codigoAgencia: string;
  nombreAgencia: string;
  idAgenciaReceptora: number;
  codigoAgenciaReceptora: string;
  nombreAgenciaReceptora: string;
  codigoLineaCredito: string;
  nombreLineaCredito: string;
  pagare: string;
  documento: string;
  asociado: string;
  fechaPagoOriginal: string;
  fechaReversion: string;
  fechaRegistroReversion: string;
  motivo: string;
  usuario: string;
  funcionario: string;
  comprobanteOriginal: string;
  comprobanteReversion: string;
  capital: number;
  interesCausado: number;
  interesIngreso: number;
  interesAnticipado: number;
  mora: number;
  seguros: number;
  aportes: number;
  papeleria: number;
  fondoGarantia: number;
  otros: number;
  valorReversado: number;
}

/** Opción de búsqueda de asociado para el informe. */
export interface InformeReversionAsociado {
  documento: string;
  nombreCompleto: string;
}

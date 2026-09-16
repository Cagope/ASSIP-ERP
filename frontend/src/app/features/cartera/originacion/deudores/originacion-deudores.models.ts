export interface SolicitudDeudorAgregarRequest {
  idSolicitudCredito: number;
  idDatosPersonal: number;
}

export interface SolicitudDeudor {
  idSolicitudDeudor: number;
  idSolicitudCredito: number;
  idDatosPersonal: number;

  tipoDeudor: 'PRINCIPAL' | 'CODEUDOR' | string;
  ordenDeudor: number;

  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;

  saldoCarteraInicio: number | null;
  diasMoraInicio: number | null;
  cumpleMoraInicio: boolean | null;

  saldoCarteraValidacion: number | null;
  diasMoraValidacion: number | null;
  cumpleMoraValidacion: boolean | null;

  activo: boolean;
}

export interface SolicitudDeudorDetalle extends SolicitudDeudor {
  fechaCreacion: string | null;
  fechaEdicion: string | null;
}

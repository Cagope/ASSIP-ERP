export type EstadoReferencias = 'PENDIENTE' | 'EN_PROCESO' | 'CERRADO';
export type TipoCierreReferencias = 'CON_REFERENCIAS' | 'SIN_REFERENCIAS';
export type MedioEntrevista = 'CELULAR' | 'FIJO' | 'WHATSAPP';

export interface SolicitudReferenciaListado {
  idSolicitudCredito: number;
  numeroSolicitud: string;
  fechaInicioSolicitud: string | null;
  fechaUltimaGestion: string | null;
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreSolicitante: string;
  idAsesor: number | null;
  usuarioAsesor: string | null;
  nombreAsesor: string | null;
  idSolicitudProceso: number;
  nombreProceso: string;
  idSolicitudResultado: number;
  nombreResultado: string;
  valorSolicitado: number | null;
  idLineaCredito: number | null;
  codigoLineaCredito: string | null;
  nombreLineaCredito: string | null;
  idSolicitudReferenciaProceso: number | null;
  estadoReferencias: EstadoReferencias;
  tipoCierre: TipoCierreReferencias | null;
  observacionCierre: string | null;
  fechaInicioReferencias: string | null;
  fechaCierre: string | null;
  fkSeguridadCierre: number | null;
  cantidadReferencias: number;
}

export interface SolicitudReferenciaParticipante {
  idSolicitudCredito: number;
  idSolicitudDeudor: number;
  idDatosPersonal: number;
  tipoDeudor: string;
  ordenDeudor: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;
  idSolicitudReferenciaProceso: number | null;
  estadoReferencias: EstadoReferencias;
  cantidadReferencias: number;
  cantidadContactadas: number;
  cantidadNoContactadas: number;
  cantidadPendientes: number;
}

export interface SolicitudReferenciaPersonal {
  idSolicitudReferenciaPersonal: number;
  idSolicitudReferenciaProceso: number;
  idSolicitudCredito: number;
  idSolicitudDeudor: number;
  nombreCompleto: string;
  telefonoCelular: string | null;
  telefonoFijo: string | null;
  medioEntrevista: MedioEntrevista | null;
  fechaHoraLlamada: string | null;
  contactoEstablecido: boolean | null;
  conceptoReferencia: string | null;
  fkSeguridadEntrevistador: number | null;
  activo: boolean;
  fkSeguridadCreacion: number | null;
  fechaCreacion: string | null;
  fkSeguridadEdicion: number | null;
  fechaEdicion: string | null;
}

export interface ReferenciaContactoRequest {
  nombreCompleto: string;
  telefonoCelular: string | null;
  telefonoFijo: string | null;
}

export interface ReferenciaEntrevistaRequest {
  medioEntrevista: MedioEntrevista;
  contactoEstablecido: boolean;
  conceptoReferencia: string | null;
}

export interface ReferenciaCierreRequest {
  tipoCierre: TipoCierreReferencias;
  observacionCierre: string | null;
}

export interface ReferenciasFiltros {
  idAgencia: number | null;
  numeroSolicitud: string;
  documento: string;
  nombreSolicitante: string;
  idAsesor: number | null;
  idSolicitudProceso: number | null;
  estadoReferencias: EstadoReferencias | '';
}

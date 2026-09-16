export interface SolicitudBien {
  idSolicitudCredito: number;
  idSolicitudDeudor: number;
  idDatosPersonal: number;
  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;
  tipoDeudor: string | null;
  ordenDeudor: number;

  idBienPersona: number;
  idBien: number;
  idTipoBien: number;
  codigoTipoBien: string | null;
  nombreTipoBien: string | null;
  descripcionGeneral: string | null;
  fechaAdquisicion: string | null;
  estadoBien: string | null;
  porcentajePropiedad: number | null;

  valorComercial: number | null;
  valorGravamen: number | null;
  valorPropiedad: number | null;
  valorGravamenPropiedad: number | null;
  valorNetoPropiedad: number | null;

  cantidadCreditosRespaldados: number | null;
  valorCreditosRespaldados: number | null;

  idSolicitudDeudorBien: number | null;
  seleccionado: boolean;
  fechaFotografia: string | null;

  porcentajeAdmisible: number | null;
  valorGarantiaAdmisible: number | null;
  valorComprometidoCreditos: number | null;
  valorGarantiaDisponible: number | null;
  valorRequeridoSolicitud: number | null;
  valorAsignadoSolicitud: number | null;
  observacion: string | null;
}

export interface SolicitudBienCreditoRespaldado {
  idBien: number;
  idCarteraCredito: number;
  idObligacionJuridica: number;
  idAgencia: number;
  idLineaCredito: number;
  pagareCartera: string | null;
  idDatosPersonal: number;
  documento: string | null;
  nombreCompleto: string | null;
  fechaDesembolso: string | null;
  valorInicialCredito: number | null;
  valorDesembolsado: number | null;
  valorCuota: number | null;
  saldoActual: number | null;
  codigoGarantiaCredito: string | null;
  descripcionGarantiaCredito: string | null;
  tipoGarantia: string | null;
  codigoEstadoCartera: string | null;
  codigoEstadoJuridico: string | null;
}

export interface SolicitudBienSeleccionRequest {
  idSolicitudDeudor: number;
  idBienPersona: number;
  seleccionado: boolean;
  observacion: string | null;
}

export interface SolicitudBienValidacion {
  puedeContinuar: boolean;
}

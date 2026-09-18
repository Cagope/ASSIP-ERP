// =========================================================
// ORIGINACIÓN DE CARTERA
// BANDEJA DE SOLICITUDES
// =========================================================


// =========================================================
// SOLICITUD LISTADO
// Alineado con SolicitudListadoDTO del backend
// =========================================================

export interface SolicitudListado {

  idSolicitudCredito: number;

  numeroSolicitud: string;

  fechaInicioSolicitud: string;

  fechaUltimaGestion: string | null;


  // =======================================================
  // AGENCIA
  // =======================================================

  idAgencia: number;

  codigoAgencia: string;

  nombreAgencia: string;


  // =======================================================
  // SOLICITANTE
  // =======================================================

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string | null;

  nombreSolicitante: string | null;


  // =======================================================
  // ASESOR
  // =======================================================

  idAsesor: number;

  usuarioAsesor: string;

  nombreAsesor: string;


  // =======================================================
  // PROCESO
  // =======================================================

  idSolicitudProceso: number;

  nombreProceso: string;


  // =======================================================
  // RESULTADO
  // =======================================================

  idSolicitudResultado: number;

  nombreResultado: string;


  // =======================================================
  // CRÉDITO
  // =======================================================

  valorSolicitado: number | null;

  idLineaCredito: number | null;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;


  // =======================================================
  // CONTROL
  // =======================================================

  activo: boolean;
}


// =========================================================
// FILTROS DE LA BANDEJA
// =========================================================

export interface SolicitudListadoFiltros {

  idAgencia: number | null;

  numeroSolicitud: string;

  documento: string;

  nombreSolicitante: string;

  idAsesor: number | null;

  idSolicitudProceso: number | null;

  idSolicitudResultado: number | null;
}

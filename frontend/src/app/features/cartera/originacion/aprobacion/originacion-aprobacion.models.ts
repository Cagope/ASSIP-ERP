// =========================================================
// ORIGINACIÓN DE CARTERA
// MODELOS DEL PROCESO DE APROBACIÓN
// =========================================================


// =========================================================
// BANDEJA DE APROBACIÓN
// =========================================================

export interface SolicitudAprobacionBandeja {

  // Solicitud
  idSolicitudCredito: number;
  numeroSolicitud: string;

  fechaInicioSolicitud: string | null;
  fechaUltimaGestion: string | null;

  // Agencia
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;

  // Asociado
  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;

  // Crédito solicitado
  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;

  valorSolicitado: number | null;
  plazoSolicitado: number | null;
  tasaColocacionAplicada: number | null;
  valorCuotaProyectada: number | null;

  // Garantía
  codigoGarantiaCredito: string | null;
  nombreGarantiaCredito: string | null;
  tipoGarantia: string | null;

  // Entes de aprobación
  idEnteFinal: number;
  nombreEnteFinal: string;

  idEnteActual: number;

}


// =========================================================
// CATÁLOGO DE DECISIONES
// =========================================================

export interface SolicitudAprobacionDecision {

  idAprobacionDecision: number;

  codigoDecision: string;
  nombreDecision: string;

}


// =========================================================
// HISTORIAL DE ACTUACIONES
// =========================================================

export interface SolicitudAprobacionActuacion {

  idSolicitudAprobacion: number;
  idSolicitudCredito: number;

  // Ente que tomó la decisión
  idEnteAprobacion: number;
  nombreEnteAprobacion: string;

  // Decisión
  idAprobacionDecision: number;
  codigoDecision: string;
  nombreDecision: string;

  // Acta
  numeroActa: string | null;
  fechaActa: string | null;

  // Concepto
  concepto: string;

  // Usuario responsable
  idUsuarioDecision: number;
  nombreUsuarioDecision: string;

  // Fecha de la actuación
  fechaDecision: string;

}


// =========================================================
// FOTOGRAFÍAS DE APROBACIÓN
//
// Se utiliza tanto para consultar las fotografías actuales
// como para consultar las fotografías históricas de una
// actuación específica.
//
// Los contenidos JSON conservan la estructura original
// generada por PostgreSQL.
// =========================================================

export interface SolicitudAprobacionFotos {

  fotoSolicitud: Record<string, unknown> | null;

  fotoDeudores: Record<string, unknown>[] | null;

  fotoFinanciero: Record<string, unknown>[] | null;

  fotoBienes: Record<string, unknown>[] | null;

  fotoCentralRiesgo: Record<string, unknown>[] | null;

  fotoAnalisis: Record<string, unknown>[] | null;

}


// =========================================================
// REGISTRAR DECISIÓN DE APROBACIÓN
// =========================================================

export interface SolicitudAprobacionDecisionRequest {

  idAprobacionDecision: number;

  concepto: string;

  numeroActa: string | null;
  fechaActa: string | null;

}


// =========================================================
// IDENTIFICADORES DE ENTES APROBADORES
// =========================================================

export const ENTES_APROBACION = {

  GERENCIA: 1,
  COMITE: 2,
  CONSEJO: 3

} as const;


// =========================================================
// CÓDIGOS DE DECISIONES
// =========================================================

export const DECISIONES_APROBACION = {

  APROBADA: 'APROBADA',
  NO_VIABLE: 'NO_VIABLE',
  SOLICITA_AJUSTES: 'SOLICITA_AJUSTES'

} as const;

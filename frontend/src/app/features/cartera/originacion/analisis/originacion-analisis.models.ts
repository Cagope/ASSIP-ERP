// =========================================================
// RESULTADO GENERAL DE LA SOLICITUD
// =========================================================

export interface SolicitudAnalisisResultado {

  // ---------------------------------------------------------
  // Solicitud
  // ---------------------------------------------------------

  idSolicitudCredito: number;
  numeroSolicitud: string;

  idAgencia: number;
  idDatosPersonal: number;

  fechaInicioSolicitud: string | null;
  fechaUltimaGestion: string | null;

  idSolicitudProceso: number;
  idSolicitudResultado: number;


  // ---------------------------------------------------------
  // Crédito
  // ---------------------------------------------------------

  idLineaCredito: number;
  codigoGarantiaCredito: string;

  valorSolicitado: number | null;
  valorCuotaProyectada: number | null;


  // ---------------------------------------------------------
  // Modelo
  // ---------------------------------------------------------

  idSolicitudModelo: number | null;
  versionModelo: string | null;

  cantidadModelosDetectados: number | null;


  // ---------------------------------------------------------
  // Población
  // ---------------------------------------------------------

  cantidadPersonas: number;
  cantidadPersonasConResultado: number;
  cantidadPersonasEvaluadas: number;
  cantidadPersonasPendientes: number;

  analisisSolicitudCompleto: boolean;

  detallePersonasPendientes: string | null;


  // ---------------------------------------------------------
  // Resultado
  // ---------------------------------------------------------

  puntajeReferenciaSolicitud: number | null;

  perfilRiesgoSolicitud: string | null;
  recomendacionSolicitud: string | null;

  cumpleOtorgamientoSolicitud: boolean | null;

  estadoAnalisisSolicitud: string | null;
  motivoResultadoSolicitud: string | null;
}


// =========================================================
// RESULTADO POR DEUDOR
// =========================================================

export interface SolicitudAnalisisDeudor {

  // ---------------------------------------------------------
  // Solicitud / deudor
  // ---------------------------------------------------------

  idSolicitudCredito: number;
  numeroSolicitud: string;

  idSolicitudDeudor: number;
  idDatosPersonal: number;

  // Identificación del participante
  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;

  tipoDeudor: string;
  ordenDeudor: number;


  // ---------------------------------------------------------
  // Modelo
  // ---------------------------------------------------------

  idSolicitudModelo: number | null;
  versionModelo: string | null;
  nombreModelo: string | null;


  // ---------------------------------------------------------
  // Estado
  // ---------------------------------------------------------

  cantidadComponentesObligatorios: number;
  cantidadComponentesEvaluados: number;
  cantidadComponentesPendientes: number;

  analisisCompleto: boolean;

  componentesPendientes: string | null;


  // ---------------------------------------------------------
  // Indicadores
  // ---------------------------------------------------------

  indicadorCapacidadPago: number | null;
  indicadorEndeudamiento: number | null;
  indicadorRazonCorriente: number | null;

  puntajeCentralFuente: number | null;
  moraMaxima24Meses: number | null;


  // ---------------------------------------------------------
  // Puntajes
  // ---------------------------------------------------------

  puntajeCapacidadPago: number | null;
  puntajeEndeudamiento: number | null;
  puntajeRazonCorriente: number | null;

  puntajeCentralCuantitativo: number | null;
  puntajeCentralCualitativo: number | null;

  puntajeGarantia: number | null;
  puntajeHabitoPagoInterno: number | null;

  puntajeTotal: number | null;


  // ---------------------------------------------------------
  // Resultado
  // ---------------------------------------------------------

  sinCapacidadPago: boolean;

  perfilRiesgo: string | null;
  recomendacion: string | null;

  cumpleOtorgamiento: boolean | null;

  motivoResultado: string | null;
}


// =========================================================
// COMPONENTE DEL ANÁLISIS
// =========================================================

export interface SolicitudAnalisisComponente {

  // ---------------------------------------------------------
  // Solicitud / deudor
  // ---------------------------------------------------------

  idSolicitudCredito: number;
  numeroSolicitud: string;

  idSolicitudDeudor: number;
  idDatosPersonal: number;

  tipoDeudor: string;
  ordenDeudor: number;


  // ---------------------------------------------------------
  // Modelo
  // ---------------------------------------------------------

  idSolicitudModelo: number | null;
  versionModelo: string | null;
  nombreModelo: string | null;


  // ---------------------------------------------------------
  // Componente
  // ---------------------------------------------------------

  idSolicitudModeloComponente: number;

  codigoComponente: string;
  nombreComponente: string;

  ordenComponente: number;

  ponderacion: number | null;

  tipoValor: string | null;
  obligatorio: boolean;


  // ---------------------------------------------------------
  // Valor evaluado
  // ---------------------------------------------------------

  valorNumericoComponente: number | null;
  valorTextoComponente: string | null;


  // ---------------------------------------------------------
  // Regla
  // ---------------------------------------------------------

  idSolicitudModeloRegla: number | null;
  ordenRegla: number | null;


  // ---------------------------------------------------------
  // Resultado
  // ---------------------------------------------------------

  puntajeObtenido: number | null;
  puntajePonderado: number | null;

  cumple: boolean | null;

  descripcionResultado: string | null;

  reglaEncontrada: boolean;
  estadoComponente: string;
}


// =========================================================
// RESPUESTA DE GUARDADO
// =========================================================

export interface SolicitudAnalisisPersistencia {

  analisisGuardados: number;
  detallesGuardados: number;

  estadoAnalisis: string;
  recomendacion: string;
}

// =========================================================
// EVALUACIÓN DE CARTERA
// =========================================================

export interface EvaluacionCartera {

  idEvaluacionCartera: number;

  fechaCorte: string;

  fechaEjecucion: string | null;

  estado: EstadoEvaluacionCartera;

  versionMetodologia: string;

  cantidadCreditos: number;

  cantidadAsociados: number;

  saldoTotalEvaluado: number;

  cantidadRecalificados: number;

  cantidadHabilitados: number;

  cantidadMantenidos: number;

  fechaComiteRiesgos: string | null;

  numeroActaRiesgos: string | null;

  fechaConsejo: string | null;

  numeroActaConsejo: string | null;

  observaciones: string | null;

  fkSeguridadCreacion: number;

  fechaCreacion: string;

  fkSeguridadEdicion: number;

  fechaEdicion: string;
}


// =========================================================
// CREAR / ACTUALIZAR
// =========================================================

export interface EvaluacionCarteraGuardar {

  fechaCorte: string;

  versionMetodologia: string;

  fechaComiteRiesgos: string | null;

  numeroActaRiesgos: string | null;

  fechaConsejo: string | null;

  numeroActaConsejo: string | null;

  observaciones: string | null;
}


// =========================================================
// RESULTADO CONSOLIDADO POR CRÉDITO
//
// Este modelo representa el resultado ya persistido de la
// evaluación.
//
// Se utiliza para:
// - listado de resultados;
// - filtros;
// - Excel;
// - cabecera de impresión individual.
// =========================================================

export interface EvaluacionCreditoResultado {

  // =======================================================
  // IDENTIFICACIÓN
  // =======================================================

  idEvaluacionCarteraCredito: number;

  idEvaluacionCartera: number;

  idCierreCarteraCredito: number;

  idCarteraCredito: number;


  // =======================================================
  // ASOCIADO
  // =======================================================

  idDatosPersonal: number;

  documento: string;

  nombres: string | null;

  primerApellido: string | null;

  segundoApellido: string | null;

  nombreCompleto: string;


  // =======================================================
  // AGENCIA
  // =======================================================

  idAgencia: number;

  codigoAgencia: string | null;

  nombreAgencia: string | null;


  // =======================================================
  // LÍNEA DE CRÉDITO
  // =======================================================

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;


  // =======================================================
  // CRÉDITO
  // =======================================================

  pagareCartera: string;

  codigoClasificacionCredito: string;

  valorInicialCredito: number;

  saldoActual: number;


  // =======================================================
  // RESULTADO DE LA EVALUACIÓN
  // =======================================================

  puntajeTotal: number;

  edadMora: string | null;

  edadRiesgoAnterior: string | null;

  edadRiesgoInicial: string | null;

  edadRiesgoCalculada: string | null;

  edadRiesgoArrastre: string | null;

  edadRiesgoFinal: string | null;


  // =======================================================
  // RECOMENDACIÓN
  // =======================================================

  accionEvaluacion: AccionEvaluacionCartera;

  descripcionAccion: string;

  comentarioEvaluacion: string | null;
}


// =========================================================
// RESULTADO DE UN CRITERIO
//
// Se utiliza para:
// - detalle individual;
// - impresión del crédito;
// - visualización de los 10 criterios.
// =========================================================

export interface EvaluacionCriterioResultado {

  // =======================================================
  // IDENTIFICACIÓN
  // =======================================================

  idEvaluacionCarteraCreditoDetalle: number;

  idEvaluacionCarteraCredito: number;


  // =======================================================
  // CRITERIO
  // =======================================================

  idEvaluacionCriterio: number;

  codigoCriterio: string;

  nombreCriterio: string;

  ordenEvaluacion: number;

  puntajeMaximo: number;


  // =======================================================
  // REGLA APLICADA
  // =======================================================

  idEvaluacionCriterioRegla: number;

  codigoRegla: string;

  nombreRegla: string;


  // =======================================================
  // RESULTADO
  // =======================================================

  valorResultado: number | null;

  codigoResultado: string | null;

  descripcionResultado: string;

  puntajeObtenido: number;

  observaciones: string | null;
}


// =========================================================
// ESTADO DE LA EVALUACIÓN
// =========================================================

export type EstadoEvaluacionCartera =
  | 'P'
  | 'D';


export const ESTADO_EVALUACION_DESCRIPCION:
Record<EstadoEvaluacionCartera, string> = {

  P: 'En proceso',

  D: 'Definitiva'

};


// =========================================================
// ACCIÓN SUGERIDA POR LA EVALUACIÓN
// =========================================================

export type AccionEvaluacionCartera =
  | 'R'
  | 'H'
  | 'M';


export const ACCION_EVALUACION_DESCRIPCION:
Record<AccionEvaluacionCartera, string> = {

  R: 'Reclasificar',

  H: 'Habilitar',

  M: 'Mantener'

};

// =========================================================
// INSUMO - FOTOGRAFÍA DE HOJA DE VIDA
//
// 1 fila = 1 asociado incluido en la evaluación.
// =========================================================

export interface EvaluacionResultadoHojaVida {

  idCierreHojaVidaPersona: number;

  idCierreHojaVida: number;

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string;

  tipoPersona: string | null;

  tieneRut: boolean | null;

  digitoVerificacion: string | null;

  nombres: string | null;

  primerApellido: string | null;

  segundoApellido: string | null;

  nombreCompleto: string;

  fechaNacimiento: string | null;

  fechaApertura: string | null;

  fechaActualizacion: string | null;

  codigoGenero: string | null;

  codigoEstadoCivil: string | null;

  codigoEscolaridad: string | null;

  cabezaFamilia: string | null;

  estratoSocial: number | null;

  codigoTipoVivienda: string | null;

  numeroHijos: number | null;

  codigoOcupacion: string | null;

  codigoSectorEconomico: string | null;

  codigoActividadSes: string | null;

  codigoActividadDian: string | null;

  direccion: string | null;

  barrio: string | null;

  telefono: string | null;

  celularUno: string | null;

  celularDos: string | null;

  correo: string | null;

  idPais: number | null;

  idDepartamento: number | null;

  idCiudad: number | null;

  idZona: number | null;

  idSubZona: number | null;

  valorSalario: number;

  valorPension: number;

  ingresosArriendo: number;

  ingresosComisiones: number;

  otrosIngresos: number;

  ingresosTotales: number;

  egresosFamiliares: number;

  egresosArriendo: number;

  egresosCredito: number;

  otrosEgresos: number;

  egresosTotales: number;

  totalActivos: number;

  totalPasivos: number;

  patrimonioTotal: number;

  deudaRelacionFinanciera: number;

  origenFondos: string | null;

  relacionFinanciera: string | null;

  ingresoDisponible: number;
}


// =========================================================
// INSUMO - MOROSIDAD DEL EXTRACTO
//
// 1 fila = 1 comprobante consolidado del crédito.
// =========================================================

export interface EvaluacionResultadoMorosidad {

  idCierreCarteraCredito: number;

  idCarteraCredito: number;

  idDatosPersonal: number;

  documento: string;

  nombres: string | null;

  primerApellido: string | null;

  segundoApellido: string | null;

  nombreCompleto: string;

  idAgencia: number | null;

  codigoAgencia: string | null;

  nombreAgencia: string | null;

  idLineaCredito: number | null;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string;

  tipoComprobante: string | null;

  numeroComprobante: string | null;

  fechaContableDesde: string | null;

  fechaContableHasta: string | null;

  valorCapital: number;

  valorMora: number;

  diasMora: number | null;
}

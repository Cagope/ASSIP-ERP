import type { SolicitudReferenciaListado, SolicitudReferenciaPersonal } from '../referencias/originacion-referencias.models';

import type {
  SolicitudAprobacionFotos,
  SolicitudAprobacionActuacion
} from '../aprobacion/originacion-aprobacion.models';


// =========================================================
// EXPEDIENTE INTEGRAL DE ORIGINACIÓN
// MODELOS PARA IMPRESIÓN
// =========================================================


// =========================================================
// MODALIDAD DE IMPRESIÓN
// =========================================================

export type OriginacionExpedienteModo =
  | 'SOLICITUD'
  | 'ACTUACION';


// =========================================================
// DOCUMENTOS DISPONIBLES
// =========================================================

export type OriginacionExpedienteDocumento =
  | 'COMPLETO'
  | 'SOLICITUD'
  | 'CODEUDORES'
  | 'FINANCIERO'
  | 'BIENES'
  | 'CENTRAL_RIESGO'
  | 'ANALISIS'
  | 'APROBACIONES'
  | 'FIRMAS';


// =========================================================
// IDENTIFICACIÓN DE LA IMPRESIÓN
// =========================================================

export interface OriginacionExpedienteIdentificacion {

  idSolicitudCredito: number;

  numeroSolicitud: string | null;

  idSolicitudAprobacion: number | null;

  fechaImpresion: string;

}


// =========================================================
// DATOS COMPLEMENTARIOS DE HOJA DE VIDA
//
// Información consultada por idDatosPersonal.
//
// No forma parte de las fotografías históricas de
// aprobación.
//
// Se conserva la estructura anterior y se incorporan
// los campos adicionales para la solicitud de crédito.
// =========================================================

export interface OriginacionExpedientePersona {

  // -------------------------------------------------------
  // IDENTIFICACIÓN
  // -------------------------------------------------------

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string | null;

  primerApellido: string | null;

  segundoApellido: string | null;

  nombres: string | null;

  nombreCompleto: string | null;


  // -------------------------------------------------------
  // VINCULACIÓN CON LA COOPERATIVA
  // -------------------------------------------------------

  fechaAfiliacion: string | null;

  cuentaAsociado: string | null;


  // -------------------------------------------------------
  // INFORMACIÓN PERSONAL
  // -------------------------------------------------------

  fechaNacimiento: string | null;

  estadoCivil: string | null;

  escolaridad: string | null;

  numeroHijos: number | null;

  personasACargo: number | null;

  nombreConyuge: string | null;


  // -------------------------------------------------------
  // CONTACTO
  // -------------------------------------------------------

  direccion: string | null;

  telefono: string | null;

  celular: string | null;

  correoElectronico: string | null;

  ciudad: string | null;

  departamento: string | null;


  // -------------------------------------------------------
  // ACTIVIDAD ECONÓMICA
  // -------------------------------------------------------

  actividadEconomica: string | null;

  sectorEconomico: string | null;

  ocupacion: string | null;

  profesion: string | null;


  // -------------------------------------------------------
  // INFORMACIÓN LABORAL
  // -------------------------------------------------------

  empresa: string | null;

  cargo: string | null;

  direccionLaboral: string | null;

  telefonoLaboral: string | null;

  celularLaboral: string | null;

  fechaVinculacionLaboral: string | null;

}


// =========================================================
// OPCIONES DEL DOCUMENTO
// =========================================================

export interface OriginacionExpedientePrintOpciones {

  documento: OriginacionExpedienteDocumento;

  incluirDatosComplementarios: boolean;

  incluirHistorialAprobaciones: boolean;

  incluirFirmasHuellas: boolean;

}


// =========================================================
// INFORMACIÓN COMPLETA PARA IMPRESIÓN
// =========================================================

export interface OriginacionExpedientePrintData {

  // -------------------------------------------------------
  // Identificación
  // -------------------------------------------------------

  identificacion: OriginacionExpedienteIdentificacion;

  // -------------------------------------------------------
  // Modalidad
  // -------------------------------------------------------

  modo: OriginacionExpedienteModo;

  // -------------------------------------------------------
  // Fotografías JSON
  //
  // Se reutiliza la interfaz real del proceso de
  // aprobación.
  // -------------------------------------------------------

  fotos: SolicitudAprobacionFotos;

  // -------------------------------------------------------
  // Historial de decisiones
  // -------------------------------------------------------

  actuaciones: SolicitudAprobacionActuacion[];

  // -------------------------------------------------------
  // Información complementaria de Hoja de Vida
  //
  // Una persona por cada idDatosPersonal.
  // -------------------------------------------------------

  personas: OriginacionExpedientePersona[];

  // Referencias vigentes de la solicitud; no son fotografía histórica.
  referencias: SolicitudReferenciaPersonal[];
  procesoReferencias: SolicitudReferenciaListado | null;

  // -------------------------------------------------------
  // Opciones de impresión
  // -------------------------------------------------------

  opciones: OriginacionExpedientePrintOpciones;

}


// =========================================================
// OPCIONES PREDETERMINADAS
// =========================================================

export const ORIGINACION_EXPEDIENTE_PRINT_OPCIONES_DEFAULT:
  OriginacionExpedientePrintOpciones = {

    documento: 'COMPLETO',

    incluirDatosComplementarios: true,

    incluirHistorialAprobaciones: true,

    incluirFirmasHuellas: true

  };


// =========================================================
// IDENTIFICACIÓN DEL SOLICITANTE O CODEUDOR
// =========================================================

export interface OriginacionExpedienteDeudorPrint {

  idSolicitudDeudor: number | null;

  idDatosPersonal: number | null;

  tipoDeudor: string | null;

  nombreCompleto: string | null;

  documento: string | null;

}


// =========================================================
// DETALLE FINANCIERO PARA IMPRESIÓN
//
// Cada concepto de ingreso y egreso se presenta
// seguido de su respectivo total.
//
// Los valores provienen de fotoFinanciero.
//
// No se recalculan las reglas del modelo de otorgamiento.
// =========================================================

export interface OriginacionExpedienteConceptoFinanciero {

  concepto: string;

  valor: number | null;

}


export interface OriginacionExpedienteGrupoFinanciero {

  titulo: string;

  conceptos: OriginacionExpedienteConceptoFinanciero[];

  total: number | null;

}

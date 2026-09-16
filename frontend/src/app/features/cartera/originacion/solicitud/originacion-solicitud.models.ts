// =========================================================
// ORIGINACIÓN DE CARTERA
// Modelos alineados con los DTO reales del backend
// =========================================================


// =========================================================
// ASOCIADO
// =========================================================

export interface OriginacionAsociado {
  idDatosPersonal: number;
  tipoDocumento: string | null;
  nombreTipoDocumento: string | null;
  documento: string | null;
  tipoPersona: string | null;

  nombres: string | null;
  primerApellido: string | null;
  segundoApellido: string | null;
  nombreCompleto: string | null;

  celularUno: string | null;
  telefono: string | null;
  correoPersonal: string | null;
  ciudadResidencia: string | null;

  fechaApertura: string | null;
  fechaActualizacion: string | null;

  diasSinActualizacion: number | null;

  // Regla parametrizada en backend - parámetro 121
  diasMaximoActualizacion: number | null;
  informacionActualizada: boolean | null;
}


// =========================================================
// CREAR SOLICITUD + GUARDAR DATOS DEL CRÉDITO
// =========================================================

export interface SolicitudCreditoCrearRequest {

  idAgencia: number;

  idDatosPersonal: number;

  idLineaCredito: number;

  codigoClasificacionCredito: string;

  codigoDestinoEconomico: string;

  codigoGarantiaCredito: string;

  codigoSubgarantia: string | null;

  idFondoGarantia: number;

  codigoFormaPago: string;

  periodoCodigoInteres: string;

  tipoModalidadInteres: string;

  amortizacionCapital: number;

  codigoTipoCuota: string;

  plazoSolicitado: number;

  mesesGraciaCapital: number;

  mesesGraciaInteres: number;

  valorSolicitado: number;

  idEmpresaLibranza: number | null;

  observacionAsesor: string | null;
}


// =========================================================
// CREAR / RETOMAR
// =========================================================

export interface SolicitudCrearRetomarRequest {

  idSolicitudCredito: number | null;

  idAgencia: number;

  idDatosPersonal: number;
}


export interface SolicitudCrearRetomarResponse {

  idSolicitudCredito: number;

  numeroSolicitud: string;

  accion: string;

  idSolicitudProceso: number;
  nombreProceso: string;

  idSolicitudResultado: number;
  nombreResultado: string;

  fechaUltimaGestion: string;
}


// =========================================================
// GUARDAR CRÉDITO
// =========================================================

export interface SolicitudCreditoGuardarRequest {

  idSolicitudCredito: number;

  idLineaCredito: number;

  codigoClasificacionCredito: string;

  codigoDestinoEconomico: string;

  codigoGarantiaCredito: string;

  codigoSubgarantia: string | null;

  idFondoGarantia: number;

  codigoFormaPago: string;

  periodoCodigoInteres: string;

  tipoModalidadInteres: string;

  amortizacionCapital: number;

  codigoTipoCuota: string;

  plazoSolicitado: number;

  mesesGraciaCapital: number;

  mesesGraciaInteres: number;

  valorSolicitado: number;

  idEmpresaLibranza: number | null;

  observacionAsesor: string | null;
}


export interface SolicitudCreditoGuardarResponse {

  idSolicitudCredito: number;

  numeroSolicitud: string;

  idCondicionInicial: number | null;

  idCondicionInicialDetalle: number | null;

  condicionEncontrada: boolean;

  plazoMinimoAplicado: number | null;

  plazoMaximoAplicado: number | null;

  cantidadSmmlvMinimoAplicada: number | null;

  cantidadSmmlvMaximoAplicada: number | null;

  valorSmmlvAplicado: number | null;

  cantidadSmmlvSolicitada: number | null;

  factorReciprocidadAportesAplicado: number | null;

  valorAportesInicio: number | null;

  cupoMaximoPorAportes: number | null;

  valorAportesRequerido: number | null;

  cumpleAportesInicio: boolean | null;

  idTasaColocacionDetalle: number | null;

  tasaColocacionAplicada: number | null;

  tasaEncontrada: boolean;

  fechaUltimaGestion: string;
}


// =========================================================
// RESUMEN
// =========================================================

export interface SolicitudCreditoResumen {

  idSolicitudCredito: number;

  numeroSolicitud: string;

  idAgencia: number;

  nombreAgencia: string | null;

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string | null;

  nombreCompleto: string | null;

  idLineaCredito: number | null;

  nombreLineaCredito: string | null;

  valorSolicitado: number | null;

  plazoSolicitado: number | null;

  idSolicitudProceso: number;

  nombreProceso: string | null;

  idSolicitudResultado: number;

  nombreResultado: string | null;

  resultadoFinal: boolean | null;

  fechaInicioSolicitud: string | null;

  fechaUltimaGestion: string | null;

  activo: boolean | null;
}


// =========================================================
// DETALLE
// =========================================================

export interface SolicitudCreditoDetalle {

  idSolicitudCredito: number;

  numeroSolicitud: string;

  fechaInicioSolicitud: string | null;

  fechaUltimaGestion: string | null;


  // Agencia

  idAgencia: number;

  codigoAgencia: string | null;

  nombreAgencia: string | null;


  // Asociado

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string | null;

  nombreCompleto: string | null;


  // Aportes

  idCuentaAportes: number | null;


  // Crédito

  idLineaCredito: number | null;

  nombreLineaCredito: string | null;

  codigoClasificacionCredito: string | null;

  nombreClasificacionCredito: string | null;

  codigoDestinoEconomico: string | null;

  nombreDestinoEconomico: string | null;


  // Garantía

  codigoGarantiaCredito: string | null;

  nombreGarantiaCredito: string | null;

  codigoSubgarantia: string | null;

  nombreSubgarantia: string | null;

  // Fondo de garantías

  idFondoGarantia: number | null;

  codigoFondoGarantia: string | null;

  nombreFondoGarantia: string | null;

  porcentajeFondoAplicado: number | null;

  formaCobroFondo: string | null;

  valorFondoGarantia: number | null;


  // Forma de pago

  codigoFormaPago: string | null;

  nombreFormaPago: string | null;


  // Modalidad

  periodoCodigoInteres: string | null;

  tipoModalidadInteres: string | null;

  nombreModalidadInteres: string | null;

  amortizacionCapital: number | null;


  // Tipo cuota

  codigoTipoCuota: string | null;

  nombreTipoCuota: string | null;


  // Plazo

  plazoSolicitado: number | null;

  mesesGraciaCapital: number | null;

  mesesGraciaInteres: number | null;

  valorSolicitado: number | null;


  // Libranza

  idEmpresaLibranza: number | null;

  nombreEmpresaLibranza: string | null;


  // Condición inicial

  idCondicionInicial: number | null;

  idCondicionInicialDetalle: number | null;

  plazoMinimoAplicado: number | null;

  plazoMaximoAplicado: number | null;

  cantidadSmmlvMinimoAplicada: number | null;

  cantidadSmmlvMaximoAplicada: number | null;

  factorReciprocidadAportesAplicado: number | null;

  valorSmmlvAplicado: number | null;

  cantidadSmmlvSolicitada: number | null;


  // Reciprocidad

  valorAportesInicio: number | null;

  cumpleAportesInicio: boolean | null;

  valorAportesValidacion: number | null;

  cumpleAportesValidacion: boolean | null;

  cupoMaximoPorAportes: number | null;

  valorAportesRequerido: number | null;


  // Tasa

  idTasaColocacionDetalle: number | null;

  tasaColocacionAplicada: number | null;

  tasaEfectivaAnual: number | null;

  valorCuotaProyectada: number | null;

  // Ente aprobador

  idEnteAprobacion: number | null;

  nombreEnteAprobacion: string | null;

  motivoAprobacion: string | null;

  esDirectivo: boolean | null;

  esPrivilegiado: boolean | null;


  // Directivo - asociado

  nombreTipoDirectivoAsociado: string | null;

  nombreCalidadDirectivoAsociado: string | null;


  // Relación privilegiada

  nombreParentesco: string | null;

  idDatosPersonalDirectivo: number | null;

  documentoDirectivo: string | null;

  nombreDirectivo: string | null;

  nombreTipoDirectivoRelacionado: string | null;

  nombreCalidadDirectivoRelacionado: string | null;


  // Límites de aprobación

  valorTopeSmmlv: number | null;

  valorTopePesos: number | null;


  // Explicación del ente aprobador

  mensajeAprobacion: string | null;

  detalleAprobacion: string | null;


  // Estado

  idSolicitudProceso: number;

  nombreProceso: string | null;

  idSolicitudResultado: number;

  nombreResultado: string | null;

  resultadoFinal: boolean | null;


  // Crédito generado

  idCarteraCredito: number | null;


  // Observación

  observacionAsesor: string | null;


  // Fechas

  fechaFinIniciada: string | null;

  fechaFinDocumentacion: string | null;

  fechaFinAprobacion: string | null;

  fechaFinDesembolso: string | null;


  // Control

  activo: boolean | null;
}


// =========================================================
// CATÁLOGOS
// =========================================================

export interface LineaCredito {

  idLineaCredito: number;

  codigoLineaCredito: string;

  nombreLineaCredito: string;

  esUtilizacionCupoTarjeta: boolean;
}

export interface ClasificacionCredito {

  codigoClasificacionCredito: string;

  descripcionClasificacionCredito: string;
}

export interface DestinoEconomico {

  codigoDestinoEconomico: string;

  descripcionDestinoEconomico: string;
}

export interface GarantiaCredito {

  codigoGarantiaCredito: string;

  descripcionGarantiaCredito: string;

  tipoGarantia: string;
}

export interface SubgarantiaCredito {

  codigoSubgarantia: string;

  descripcionSubgarantia: string;
}

export interface FormaPago {

  codigoFormaPago: string;

  descripcionFormaPago: string;
}

export interface ModalidadInteres {

  periodoCodigo: string;

  tipoModalidad: string;

  descripcionModalidadInteres: string;

  periodoMeses: number;
}

export interface TipoCuota {

  codigoTipoCuota: string;

  descripcionTipoCuota: string;
}

export interface CentralRiesgoCatalogo {

  idCentralRiesgo: number;

  codigoCentral: string;

  documentoCentral: string;

  nombreCentral: string;

  descripcion: string | null;
}

export interface FondoGarantia {

  idFondoGarantia: number;

  codigoFondo: string;

  nombreFondo: string;

  porcentajeFondo: number;

  activo: boolean;
}

// =========================================================
// RESUMEN CENTRAL DE RIESGO POR DEUDOR
// =========================================================

export interface SolicitudCentralRiesgo {
  idSolicitudDeudorCentral: number | null;
  idSolicitudDeudor: number;
  idSolicitudCredito: number;

  idDatosPersonal: number;

  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;

  tipoDeudor: string | null;
  ordenDeudor: number;

  idCentralRiesgo: number | null;
  codigoCentral: string | null;
  nombreCentral: string | null;

  fechaConsulta: string | null;

  // Internamente corresponde a fecha_fotografia.
  // En la UI se mostrará como última actualización / fecha de registro.
  fechaFotografia: string | null;

  saldoActualObligaciones: number | null;
  valorCuotasMensuales: number | null;

  puntajeCentral: number | null;

  /**
   * Estado técnico de la consulta:
   *
   * CON_HISTORIAL
   * SIN_HISTORIAL
   */
  calificacionCentral: string | null;

  /**
   * Clasificación cualitativa usada por el modelo:
   *
   * CON HISTORIAL - OK
   * CON PERMANENCIAS
   * CON REPORTES
   * SIN HISTORIAL
   */
  calificacionCualitativa: string | null;

  activo: boolean | null;
}


// =========================================================
// DETALLE CENTRAL DE RIESGO
// =========================================================

export interface SolicitudCentralRiesgoDetalle {

  // ---------------------------------------------------------
  // Solicitud / deudor
  // ---------------------------------------------------------

  idSolicitudDeudorCentral: number | null;
  idSolicitudDeudor: number;
  idSolicitudCredito: number;

  idDatosPersonal: number;

  tipoDocumento: string | null;
  documento: string | null;
  nombreCompleto: string | null;

  tipoDeudor: string | null;
  ordenDeudor: number;


  // ---------------------------------------------------------
  // Central
  // ---------------------------------------------------------

  idCentralRiesgo: number;
  codigoCentral: string | null;
  documentoCentral: string | null;
  nombreCentral: string | null;
  descripcionCentral: string | null;


  // ---------------------------------------------------------
  // Consulta / registro
  // ---------------------------------------------------------

  fechaConsulta: string | null;
  fechaFotografia: string | null;


  // ---------------------------------------------------------
  // Obligaciones
  // ---------------------------------------------------------

  valorInicialObligaciones: number | null;
  saldoActualObligaciones: number | null;
  valorCuotasMensuales: number | null;


  // ---------------------------------------------------------
  // Calificaciones
  // ---------------------------------------------------------

  cantidadCalificacionA: number | null;
  cantidadCalificacionB: number | null;
  cantidadCalificacionC: number | null;
  cantidadCalificacionD: number | null;
  cantidadCalificacionE: number | null;
  cantidadCalificacionK: number | null;


  // ---------------------------------------------------------
  // Eventos
  // ---------------------------------------------------------

  cantidadReestructuraciones: number | null;
  cantidadRefinanciaciones: number | null;
  cantidadCuentasEmbargadas: number | null;


  // ---------------------------------------------------------
  // Resultado central
  // ---------------------------------------------------------

  puntajeCentral: number | null;

  /**
   * Estado técnico:
   *
   * CON_HISTORIAL
   * SIN_HISTORIAL
   */
  calificacionCentral: string | null;

  /**
   * Clasificación cualitativa del modelo:
   *
   * CON HISTORIAL - OK
   * CON PERMANENCIAS
   * CON REPORTES
   * SIN HISTORIAL
   */
  calificacionCualitativa: string | null;

  observacion: string | null;


  // ---------------------------------------------------------
  // Control
  // ---------------------------------------------------------

  activo: boolean | null;

  fechaCreacion: string | null;
  fechaEdicion: string | null;
}


// =========================================================
// REQUEST GUARDAR / ACTUALIZAR CENTRAL DE RIESGO
// =========================================================

export interface SolicitudCentralRiesgoGuardarRequest {

  // ---------------------------------------------------------
  // Deudor / central
  // ---------------------------------------------------------

  idSolicitudDeudor: number;
  idCentralRiesgo: number;

  fechaConsulta: string;


  // ---------------------------------------------------------
  // Obligaciones
  // ---------------------------------------------------------

  valorInicialObligaciones: number | null;
  saldoActualObligaciones: number | null;
  valorCuotasMensuales: number | null;


  // ---------------------------------------------------------
  // Calificaciones
  // ---------------------------------------------------------

  cantidadCalificacionA: number | null;
  cantidadCalificacionB: number | null;
  cantidadCalificacionC: number | null;
  cantidadCalificacionD: number | null;
  cantidadCalificacionE: number | null;
  cantidadCalificacionK: number | null;


  // ---------------------------------------------------------
  // Eventos
  // ---------------------------------------------------------

  cantidadReestructuraciones: number | null;
  cantidadRefinanciaciones: number | null;
  cantidadCuentasEmbargadas: number | null;


  // ---------------------------------------------------------
  // Resultado central
  // ---------------------------------------------------------

  puntajeCentral: number | null;

  /**
   * Estado técnico de la consulta:
   *
   * CON_HISTORIAL
   * SIN_HISTORIAL
   */
  calificacionCentral: string | null;

  /**
   * Clasificación cualitativa:
   *
   * CON HISTORIAL - OK
   * CON PERMANENCIAS
   * CON REPORTES
   * SIN HISTORIAL
   */
  calificacionCualitativa: string | null;

  observacion: string | null;
}


// =========================================================
// CATÁLOGO DE CENTRALES DE RIESGO
// =========================================================

export interface CentralRiesgoCatalogo {
  idCentralRiesgo: number;
  codigoCentral: string | null;
  documentoCentral: string | null;
  nombreCentral: string;
  descripcion: string | null;
}

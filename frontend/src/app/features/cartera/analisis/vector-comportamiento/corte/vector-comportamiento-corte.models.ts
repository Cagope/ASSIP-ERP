// =========================================================
// VECTOR DE COMPORTAMIENTO POR CORTE - RESUMEN
//
// 1 fila = 1 crédito con saldo > 0
// en la fecha de corte seleccionada.
//
// REGLA:
//
// La fecha seleccionada se trata como la posición ACTUAL
// de referencia del Vector.
//
// Los indicadores históricos se calculan exclusivamente
// con máximo 12 cierres anteriores.
//
// Toda la información financiera corresponde al corte.
// No utiliza el saldo actual del maestro para determinar
// la población.
// =========================================================

export interface VectorComportamientoCorteResumen {

  // =======================================================
  // IDENTIFICACIÓN
  // =======================================================

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string;

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string;

  nombreCompleto: string;

  telefono: string | null;

  celular: string | null;

  correo: string | null;


  // =======================================================
  // CORTE SELECCIONADO / VENTANA HISTÓRICA
  // =======================================================

  /**
   * Fecha seleccionada por el usuario.
   *
   * Esta fecha constituye la posición ACTUAL
   * de referencia del Vector por Corte.
   */
  fechaCorte: string;

  /**
   * Corte histórico más antiguo dentro
   * de la ventana máxima de 12 cierres anteriores.
   */
  primerCorte: string | null;

  /**
   * Corte histórico inmediatamente anterior
   * al corte seleccionado.
   *
   * NO corresponde al corte seleccionado.
   */
  ultimoCorte: string | null;

  /**
   * Cantidad de cierres históricos anteriores
   * observados.
   *
   * Rango:
   * 0..12
   *
   * No incluye el corte seleccionado.
   */
  cantidadCortesObservados: number;


  // =======================================================
  // COMPORTAMIENTO HISTÓRICO DE MORA
  // =======================================================

  /**
   * Días de mora en el corte seleccionado.
   */
  moraUltimoCorte: number;

  /**
   * Máxima mora encontrada exclusivamente
   * en los cierres históricos anteriores.
   */
  moraMaxima: number;

  /**
   * Cantidad de cierres históricos anteriores
   * sin mora.
   */
  cantidadCortesAlDia: number;

  /**
   * Cantidad de cierres históricos anteriores
   * con mora.
   */
  cantidadCortesConMora: number;

  cantidadMora1_30: number;

  cantidadMora31_60: number;

  cantidadMora61_90: number;

  cantidadMora91_120: number;

  cantidadMora121_150: number;

  cantidadMora151_180: number;

  cantidadMora181_360: number;

  cantidadMoraMayor360: number;

  /**
   * Porcentaje histórico de comportamiento:
   *
   * cortes con mora
   * -------------------- x 100
   * cortes observados
   */
  pbbMora: number;


  // =======================================================
  // ORIGINACIÓN
  // =======================================================

  valorInicialCredito: number;

  valorDesembolsado: number;


  // =======================================================
  // SALDOS
  // =======================================================

  /**
   * Saldo del corte histórico más antiguo
   * de la ventana observada.
   */
  saldoPrimerCorte: number;

  /**
   * Saldo del crédito en la fecha seleccionada.
   *
   * Es el equivalente funcional de saldoActualMaestro
   * en el Vector Actual.
   */
  saldoUltimoCorte: number;

  variacionSaldoPeriodo: number;


  // =======================================================
  // SEVERIDAD
  //
  // saldo al corte / valor desembolsado * 100
  // =======================================================

  severidad: number;

  /**
   * Clasificación utilizada para consolidar
   * la distribución general de severidad.
   *
   * Valores esperados:
   *
   * SEVERIDAD 0-10
   * SEVERIDAD 10-25
   * SEVERIDAD 25-50
   * SEVERIDAD 50-75
   * SEVERIDAD 75-100
   */
  rangoSeveridad: string | null;


  // =======================================================
  // ESTADO EN EL CORTE SELECCIONADO
  // =======================================================

  codigoEstadoCartera: string | null;

  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;

  descripcionEstadoJuridico: string | null;

  codigoClasificacionCredito: string | null;

  descripcionClasificacionCredito: string | null;


  // =======================================================
  // EDADES EN EL CORTE SELECCIONADO
  // =======================================================

  edadRiesgoInicialResultado: string | null;

  edadDeMoraResultado: string | null;

  edadDeRiesgoResultado: string | null;

  edadDePeResultado: string | null;

  edadDeHomologacionResultado: string | null;

  edadContableResultado: string | null;


  // =======================================================
  // MODELO / PÉRDIDA ESPERADA
  // =======================================================

  codigoMetodoCalculo: string | null;

  vea: number | null;

  pi: number | null;

  pdi: number | null;

  perdidaEsperada: number | null;


  // =======================================================
  // DETERIOROS
  // =======================================================

  deterioroCapital: number | null;

  deterioroIntereses: number | null;

  deterioroOtros: number | null;

  deterioroTotal: number | null;


  // =======================================================
  // INDICADORES
  // =======================================================

  /**
   * TRUE cuando existió mora en alguno
   * de los cierres históricos anteriores.
   */
  tuvoMoraPeriodo: boolean;

  /**
   * TRUE cuando el crédito presenta mora
   * precisamente en el corte seleccionado.
   */
  estaEnMoraUltimoCorte: boolean;

  /**
   * TRUE cuando la mora máxima histórica
   * anterior supera 90 días.
   */
  tuvoMoraMayor90: boolean;


  // =======================================================
  // IDENTIFICADORES DEL CORTE SELECCIONADO
  // =======================================================

  idCierreCartera: number;

  idCierreCarteraCredito: number;

  idCierreCarteraResultado: number;
}


// =========================================================
// VECTOR DE COMPORTAMIENTO POR CORTE - DETALLE
//
// 1 fila = 1 posición del Vector.
//
// POSICIONES:
//
// 1
//   = corte seleccionado.
//   = tipoPosicion ACTUAL.
//
// 2..13
//   = máximo 12 cierres anteriores.
//   = tipoPosicion CIERRE.
//
// La población corresponde exclusivamente a créditos
// con saldo > 0 en la fecha seleccionada.
// =========================================================

export interface VectorComportamientoCorteDetalle {

  // =======================================================
  // IDENTIFICACIÓN
  // =======================================================

  idCarteraCredito: number;

  idAgencia: number;

  idLineaCredito: number;

  codigoLineaCredito: string | null;

  nombreLineaCredito: string | null;

  pagareCartera: string;

  idDatosPersonal: number;

  tipoDocumento: string | null;

  documento: string;

  nombreCompleto: string;


  // =======================================================
  // CORTE SELECCIONADO
  // =======================================================

  /**
   * Fecha seleccionada por el usuario.
   *
   * Es común a todas las posiciones
   * del Vector consultado.
   */
  fechaCorteSeleccionado: string;


  // =======================================================
  // POSICIÓN DEL VECTOR
  // =======================================================

  /**
   * Posición dentro del Vector.
   *
   * 1    = ACTUAL / corte seleccionado.
   * 2..13 = cierres anteriores.
   */
  posicionVector: number;

  /**
   * ACTUAL
   * CIERRE
   */
  tipoPosicion: string;

  /**
   * ACTUAL para la posición 1.
   *
   * yyyy-MM para los cierres anteriores.
   */
  periodoVector: string;

  /**
   * Fecha efectiva correspondiente
   * a la posición del Vector.
   */
  fechaReferencia: string;


  // =======================================================
  // IDENTIFICADORES DEL CIERRE
  // =======================================================

  idCierreCartera: number;

  idCierreCarteraCredito: number;

  idCierreCarteraResultado: number;


  // =======================================================
  // CORTE / PERÍODO
  //
  // Se conservan estos campos porque forman parte
  // del DTO histórico y pueden ser utilizados
  // en tablas, reportes o trazabilidad.
  // =======================================================

  fechaCorte: string;

  anioCorte: number;

  mesCorte: number;

  periodoCorte: string;


  // =======================================================
  // ESTADO DEL CRÉDITO EN LA POSICIÓN
  // =======================================================

  codigoEstadoCartera: string | null;

  descripcionEstadoCartera: string | null;

  codigoEstadoJuridico: string | null;

  descripcionEstadoJuridico: string | null;

  codigoClasificacionCredito: string | null;

  descripcionClasificacionCredito: string | null;


  // =======================================================
  // MORA
  // =======================================================

  diasMora: number;

  rangoMora: string;

  ordenRangoMora: number;

  tieneMora: boolean;


  // =======================================================
  // EDADES
  // =======================================================

  edadRiesgoInicialResultado: string | null;

  edadDeMoraResultado: string | null;

  edadDeRiesgoResultado: string | null;

  edadDePeResultado: string | null;

  edadDeHomologacionResultado: string | null;

  edadContableResultado: string | null;


  // =======================================================
  // ORIGINACIÓN
  // =======================================================

  valorInicialCredito: number;

  valorDesembolsado: number;


  // =======================================================
  // SALDOS DE LA POSICIÓN
  // =======================================================

  saldoActualFotografia: number;

  saldoActualResultado: number;

  saldoCreditoFechaCorte: number;


  // =======================================================
  // MODELO / PÉRDIDA ESPERADA
  // =======================================================

  codigoMetodoCalculo: string | null;

  vea: number | null;

  pi: number | null;

  pdi: number | null;

  perdidaEsperada: number | null;


  // =======================================================
  // DETERIOROS
  // =======================================================

  deterioroCapital: number | null;

  deterioroIntereses: number | null;

  deterioroOtros: number | null;

  deterioroTotal: number | null;


  // =======================================================
  // APORTES
  // =======================================================

  saldoAportesFechaCorte: number | null;

  porcentajeAportesCredito: number | null;

  valorAportesCredito: number | null;


  // =======================================================
  // GARANTÍAS
  // =======================================================

  cantidadBienesGarantia: number | null;

  valorGarantiasTotal: number | null;

  porcentajeGarantiasCredito: number | null;

  valorGarantiasCredito: number | null;
}

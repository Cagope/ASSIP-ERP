export type TipoEdadRiesgo = 'CONTABLE' | 'MORA' | 'RIESGO' | 'PE' | 'HOMOLOGACION';
export type DimensionRiesgo =
  | 'AGENCIA'
  | 'LINEA'
  | 'CLASIFICACION'
  | 'GARANTIA'
  | 'DESTINO'
  | 'ESTADO_JURIDICO'
  | 'MODIFICACION'
  | 'METODO_CALCULO';
export type CriterioConcentracion = 'SALDO' | 'PERDIDA_ESPERADA' | 'DETERIORO' | 'EXPOSICION';

export interface RiesgoDeterioroResumen {
  fechaCorte: string;
  cantidadCreditos: number;
  saldoCartera: number;
  vea: number;
  exposicionTotal: number;
  perdidaEsperada: number;
  deterioroCapital: number;
  deterioroIntereses: number;
  deterioroOtros: number;
  deterioroTotal: number;
  porcentajePerdidaEsperadaSobreVea: number;
  porcentajeDeterioroSobreSaldo: number;
  cantidadMora30: number;
  saldoMora30: number;
  porcentajeSaldoMora30: number;
  cantidadMora60: number;
  saldoMora60: number;
  porcentajeSaldoMora60: number;
  cantidadMora90: number;
  saldoMora90: number;
  porcentajeSaldoMora90: number;
  cantidadMora180: number;
  saldoMora180: number;
  porcentajeSaldoMora180: number;
}

export interface RiesgoDeterioroEdad {
  edad: string;
  cantidadCreditos: number;
  saldoCartera: number;
  porcentajeSaldo: number;
  vea: number;
  exposicionTotal: number;
  piPromedioPonderado: number;
  pdiPromedioPonderado: number;
  perdidaEsperada: number;
  deterioroCapital: number;
  deterioroIntereses: number;
  deterioroOtros: number;
  deterioroTotal: number;
}

export interface RiesgoDeterioroSegmento {
  codigo: string;
  descripcion: string;
  cantidadCreditos: number;
  saldoCartera: number;
  porcentajeSaldo: number;
  vea: number;
  exposicionTotal: number;
  perdidaEsperada: number;
  deterioroCapital: number;
  deterioroIntereses: number;
  deterioroOtros: number;
  deterioroTotal: number;
  porcentajeDeterioroSobreSaldo: number;
}

export interface RiesgoDeterioroEvolucion {
  fechaCorte: string;
  cantidadCreditos: number;
  saldoCartera: number;
  vea: number;
  exposicionTotal: number;
  perdidaEsperada: number;
  deterioroTotal: number;
  saldoMora30: number;
  saldoMora60: number;
  saldoMora90: number;
  saldoMora180: number;
  porcentajeMora30: number;
  porcentajeMora60: number;
  porcentajeMora90: number;
  porcentajeMora180: number;
  variacionSaldo: number;
  variacionSaldoPorcentaje: number;
  variacionPerdidaEsperada: number;
  variacionDeterioro: number;
  variacionDeterioroPorcentaje: number;
}

export interface RiesgoDeterioroConcentracion {
  posicion: number;
  idCarteraCredito: number;
  idDatosPersonal: number;
  idAgencia: number;
  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;
  pagareCartera: string;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;
  saldoCartera: number;
  diasMora: number;
  edadContable: string;
  vea: number;
  pi: number;
  pdi: number;
  perdidaEsperada: number;
  deterioroTotal: number;
  exposicionTotal: number;
  valorAportesCredito: number;
  valorGarantiasCredito: number;
  porcentajeSobreTotal: number;
  porcentajeAcumulado: number;
}

export interface RiesgoDeterioroDetalle {
  idCarteraCredito: number;
  idCierreCarteraCredito: number;
  idAgencia: number;
  idLineaCredito: number;
  codigoLineaCredito: string;
  nombreLineaCredito: string;
  pagareCartera: string;
  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;
  codigoClasificacionCredito: string;
  descripcionClasificacionCredito: string;
  codigoGarantiaCredito: string;
  descripcionGarantiaCredito: string;
  tipoGarantia: string;
  codigoDestinoEconomico: string;
  descripcionDestinoEconomico: string;
  codigoEstadoCartera: string;
  descripcionEstadoCartera: string;
  codigoEstadoJuridico: string;
  descripcionEstadoJuridico: string;
  codigoModificacionCredito: string;
  descripcionModificacionCredito: string;
  esReestructurado: boolean;
  fechaDesembolso: string;
  fechaCorte: string;
  valorInicialCredito: number;
  valorDesembolsado: number;
  saldoCartera: number;
  diasMora: number;
  edadRiesgoInicial: string;
  edadMora: string;
  edadRiesgo: string;
  edadPe: string;
  edadHomologacion: string;
  edadContable: string;
  saldoAportesFechaCorte: number;
  porcentajeAportesCredito: number;
  valorAportesCredito: number;
  cantidadBienesGarantia: number;
  valorGarantiasTotal: number;
  porcentajeGarantiasCredito: number;
  valorGarantiasCredito: number;
  vea: number;
  saldoInteresesCausados: number;
  saldoInteresesContingentes: number;
  valorCostasJudiciales: number;
  saldoSeguros: number;
  saldoAlivios: number;
  valorFondosGarantias: number;
  valorOtrosConceptos: number;
  pi: number;
  pdi: number;
  perdidaEsperada: number;
  deterioroCapital: number;
  deterioroIntereses: number;
  deterioroOtros: number;
  deterioroTotal: number;
  exposicionTotal: number;
}

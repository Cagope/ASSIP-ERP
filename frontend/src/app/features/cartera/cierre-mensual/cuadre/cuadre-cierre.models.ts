// =========================================================
// RESULTADO DEL CUADRE CONSOLIDADO FINAL
//
// Corresponde a:
//
// GET
// /api/v1/cartera/cierre-mensual/cuadre/{idCierreCartera}
//
// =========================================================

export interface CuadreCierre {

  // =======================================================
  // IDENTIFICACIÓN
  // =======================================================

  idCierreCartera: number;

  estado: string;

  cuadrado: boolean;


  // =======================================================
  // CANTIDADES
  // =======================================================

  cantidadCreditosTotal: number;

  cantidadCreditosA1: number;

  cantidadCreditosPe: number;


  // =======================================================
  // SALDO ACTUAL
  // =======================================================

  saldoActualTotal: number;

  saldoActualA1: number;

  saldoActualPe: number;


  // =======================================================
  // INTERESES CAUSADOS
  // =======================================================

  saldoInteresesCausadosTotal: number;

  saldoInteresesCausadosA1: number;

  saldoInteresesCausadosPe: number;

  valorInteresesCausadosMesTotal: number;

  valorInteresesCausadosMesA1: number;

  valorInteresesCausadosMesPe: number;


  // =======================================================
  // INTERESES CONTINGENTES
  // =======================================================

  saldoInteresesContingentesTotal: number;

  saldoInteresesContingentesA1: number;

  saldoInteresesContingentesPe: number;

  valorInteresesContingentesMesTotal: number;

  valorInteresesContingentesMesA1: number;

  valorInteresesContingentesMesPe: number;


  // =======================================================
  // SEGUROS
  // =======================================================

  saldoSegurosTotal: number;

  saldoSegurosA1: number;

  saldoSegurosPe: number;

  valorSegurosMesTotal: number;

  valorSegurosMesA1: number;

  valorSegurosMesPe: number;


  // =======================================================
  // ALIVIOS
  // =======================================================

  saldoAliviosTotal: number;

  saldoAliviosA1: number;

  saldoAliviosPe: number;

  valorAliviosMesTotal: number;

  valorAliviosMesA1: number;

  valorAliviosMesPe: number;


  // =======================================================
  // COSTAS JUDICIALES
  // =======================================================

  valorCostasJudicialesTotal: number;

  valorCostasJudicialesA1: number;

  valorCostasJudicialesPe: number;


  // =======================================================
  // APORTES
  // =======================================================

  saldoAportesFechaCorteTotal: number;

  saldoAportesFechaCorteA1: number;

  saldoAportesFechaCortePe: number;

  valorAportesCreditoTotal: number;

  valorAportesCreditoA1: number;

  valorAportesCreditoPe: number;


  // =======================================================
  // GARANTÍAS
  // =======================================================

  valorGarantiasTotal: number;

  valorGarantiasA1: number;

  valorGarantiasPe: number;

  valorGarantiasCreditoTotal: number;

  valorGarantiasCreditoA1: number;

  valorGarantiasCreditoPe: number;


  // =======================================================
  // OTROS CONCEPTOS
  // =======================================================

  valorFondosGarantiasTotal: number;

  valorFondosGarantiasA1: number;

  valorFondosGarantiasPe: number;

  valorOtrosConceptosTotal: number;

  valorOtrosConceptosA1: number;

  valorOtrosConceptosPe: number;


  // =======================================================
  // DETERIOROS
  // =======================================================

  deterioroCapitalTotal: number;

  deterioroCapitalA1: number;

  deterioroCapitalPe: number;

  deterioroInteresesTotal: number;

  deterioroInteresesA1: number;

  deterioroInteresesPe: number;

  deterioroOtrosTotal: number;

  deterioroOtrosA1: number;

  deterioroOtrosPe: number;


  // =======================================================
  // PÉRDIDA ESPERADA
  // =======================================================

  perdidaEsperadaTotal: number;

  perdidaEsperadaA1: number;

  perdidaEsperadaPe: number;


  // =======================================================
  // DIFERENCIAS GENERALES
  // =======================================================

  diferenciaSaldoActual: number;

  diferenciaInteresesCausados: number;

  diferenciaInteresesContingentes: number;

  diferenciaSeguros: number;

  diferenciaAlivios: number;

  diferenciaCostasJudiciales: number;

  diferenciaAportes: number;

  diferenciaGarantias: number;

  diferenciaFondosGarantias: number;

  diferenciaOtrosConceptos: number;

  diferenciaDeterioroCapital: number;

  diferenciaDeterioroIntereses: number;

  diferenciaDeterioroOtros: number;

  diferenciaPerdidaEsperada: number;


  // =======================================================
  // PE DETALLE
  // =======================================================

  perdidaEsperadaPeDetalle: number;

  deterioroCapitalPeDetalle: number;

  deterioroInteresesPeDetalle: number;

  deterioroOtrosPeDetalle: number;

  deterioroTotalPeDetalle: number;


  // =======================================================
  // DIFERENCIAS PE
  // =======================================================

  diferenciaPerdidaEsperadaPe: number;

  diferenciaDeterioroCapitalPe: number;

  diferenciaDeterioroInteresesPe: number;

  diferenciaDeterioroOtrosPe: number;


  // =======================================================
  // MENSAJE
  // =======================================================

  mensaje: string;
}

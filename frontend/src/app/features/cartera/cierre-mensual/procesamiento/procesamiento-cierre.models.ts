// =========================================================
// RESULTADO DEL PROCESAMIENTO COMPLETO DEL CIERRE
//
// Corresponde a la respuesta de:
//
// POST
// /api/v1/cartera/cierre-mensual/{idCierreCartera}/procesar-calculos
//
// =========================================================

export interface ResultadoProcesamientoCierre {

  // =======================================================
  // IDENTIFICACIÓN DEL CIERRE
  // =======================================================

  idCierreCartera: number;

  fechaCorte: string;

  estadoCierre: string;


  // =======================================================
  // PROCESOS PREVIOS
  // =======================================================

  interesesCausados: number;

  segurosCausados: number;

  aliviosConsolidados: number;

  calculosPrevios: number;


  // =======================================================
  // ANEXO 1
  // =======================================================

  anexo1EdadesContables: number;

  anexo1DeteriorosCapital: number;

  anexo1DeteriorosIntereses: number;


  // =======================================================
  // ANEXO 2 / PÉRDIDA ESPERADA
  // =======================================================

  poblacionAnexo2: number;

  resultadosAnexo2Persistidos: number;


  // =======================================================
  // VALIDACIONES FINALES
  // =======================================================

  validacionesFinalesOk: boolean;

  estadoValidacionFinal: string;


  // =======================================================
  // CONTROL DE POBLACIÓN
  // =======================================================

  cantidadCreditosCabecera: number;

  cantidadCreditosFotografia: number;

  cantidadResultados: number;

  cantidadResultadosA1: number;

  cantidadResultadosPe: number;

  cantidadPePersistidos: number;


  // =======================================================
  // ESTADO PE
  // =======================================================

  procesoPeFinalizado: boolean;


  // =======================================================
  // ADVERTENCIAS
  // =======================================================

  advertencias: string[];
}

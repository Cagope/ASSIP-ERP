// =========================================================
// RESULTADO DE VALIDACIÓN FINAL DEL CIERRE
//
// Corresponde a:
//
// GET
// /api/v1/cartera/cierre-mensual/validacion/{idCierreCartera}
//
// =========================================================

export interface ResultadoValidacionCierre {

  // =======================================================
  // IDENTIFICACIÓN Y RESULTADO GENERAL
  // =======================================================

  idCierreCartera: number;

  valido: boolean;

  estado: string;

  mensaje: string;


  // =======================================================
  // CANTIDADES PRINCIPALES
  // =======================================================

  cantidadCreditosCabecera: number;

  cantidadCreditosFotografia: number;

  cantidadResultados: number;

  cantidadResultadosA1: number;

  cantidadResultadosPe: number;

  cantidadPePersistidos: number;


  // =======================================================
  // VALIDACIONES DE INTEGRIDAD
  // =======================================================

  creditosSinResultado: number;

  creditosConResultadoDuplicado: number;

  metodosInvalidos: number;

  peSinResultadoPe: number;

  peDuplicados: number;

  peHuerfanos: number;


  // =======================================================
  // VALIDACIONES DE EDADES
  // =======================================================

  edadesNulas: number;

  edadesInvalidas: number;

  edadesPeNulas: number;

  edadesPeInvalidas: number;


  // =======================================================
  // VALIDACIONES MONETARIAS
  // =======================================================

  valoresNegativos: number;

  valoresPeNegativos: number;

  piPdiFueraRango: number;

  piPdiPeFueraRango: number;

  peDescuadrados: number;


  // =======================================================
  // PROCESO PE
  // =======================================================

  procesoPeFinalizado: boolean;


  // =======================================================
  // CUADRE CONSOLIDADO
  // =======================================================

  cuadreConsolidadoOk: boolean;

  estadoCuadreConsolidado: string;


  // =======================================================
  // DETALLE
  // =======================================================

  errores: string[];

  advertencias: string[];
}

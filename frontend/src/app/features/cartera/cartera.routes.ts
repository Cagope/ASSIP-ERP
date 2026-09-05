import { Routes } from '@angular/router';

// =============================
// CONSULTA DE CRÉDITOS
// =============================
import { CONSULTA_CREDITOS_ROUTES } from './consulta-creditos/consulta-creditos.routes';

// =============================
// CIERRE MENSUAL
// =============================
import { CIERRE_MENSUAL_CARTERA_ROUTES } from './cierre-mensual/cierre-mensual-cartera.routes';

// =============================
// EVALUACIÓN DE CARTERA
// =============================
import { EVALUACION_CARTERA_ROUTES } from './evaluacion/evaluaciones/evaluacion-cartera.routes';

// =============================
// RESULTADOS CENTRAL DE RIESGOS
// =============================
import { CENTRAL_RIESGO_RESULTADO_ROUTES } from './evaluacion/central-riesgos/resultados/central-riesgo-resultado.routes';

// =============================
// VECTOR DE COMPORTAMIENTO - ACTUAL
// =============================
import { VECTOR_COMPORTAMIENTO_ROUTES } from './analisis/vector-comportamiento/actual/vector-comportamiento.routes';

// =============================
// VECTOR DE COMPORTAMIENTO - CORTE
// =============================
import { VECTOR_COMPORTAMIENTO_CORTE_ROUTES } from './analisis/vector-comportamiento/corte/vector-comportamiento-corte.routes';

// =============================
// MATRIZ DE RODAMIENTO
// =============================
import { MATRIZ_RODAMIENTO_ROUTES } from './analisis/matriz-rodamiento/matriz-rodamiento.routes';

// =============================
// ANÁLISIS DE COSECHAS
// =============================
import { COSECHAS_ROUTES } from './analisis/cosechas/cosechas.routes';

// =============================
// ANÁLISIS DE RIESGO Y DETERIORO
// =============================
import { RIESGO_DETERIORO_ROUTES } from './analisis/riesgo-deterioro/riesgo-deterioro.routes';

// =============================
// MORA TEMPRANA / CALIDAD DE ORIGINACIÓN
// =============================
import { MORA_TEMPRANA_ROUTES } from './analisis/mora-temprana/mora-temprana.routes';

// =============================
// CURACIÓN Y REINCIDENCIA
// =============================
import { CURACION_REINCIDENCIA_ROUTES } from './analisis/curacion-reincidencia/curacion-reincidencia.routes';

// =============================
// CONCENTRACIÓN DE CARTERA
// =============================
import { CONCENTRACION_CARTERA_ROUTES } from './analisis/concentracion-cartera/concentracion-cartera.routes';

// =============================
// GARANTÍAS Y COBERTURA
// =============================
import { GARANTIAS_COBERTURA_ROUTES } from './analisis/garantias-cobertura/garantias-cobertura.routes';

// =============================
// RECIPROCIDAD DE APORTES
// =============================
import { RECIPROCIDAD_APORTES_ROUTES } from './analisis/reciprocidad-aportes/reciprocidad-aportes.routes';

// =============================
// CANCELACIÓN Y PREPAGO
// =============================
import { CANCELACION_PREPAGO_ROUTES } from './analisis/cancelacion-prepago/cancelacion-prepago.routes';

// =============================
// ROLL FORWARD
// =============================
import { ROLL_FORWARD_ROUTES } from './analisis/roll-forward/roll-forward.routes';

export const CARTERA_ROUTES: Routes = [

  // =============================
  // CONSULTA DE CRÉDITOS
  // =============================
  {
    path: 'consulta-creditos',
    children: CONSULTA_CREDITOS_ROUTES
  },

  // =============================
  // CIERRE MENSUAL
  // =============================
  {
    path: 'cierre-mensual',
    children: CIERRE_MENSUAL_CARTERA_ROUTES
  },

  // =============================
  // EVALUACIÓN DE CARTERA
  // =============================
  {
    path: 'evaluacion/evaluaciones',
    children: EVALUACION_CARTERA_ROUTES
  },

  // =============================
  // RESULTADOS CENTRAL DE RIESGOS
  // =============================
  {
    path: 'evaluacion/central-riesgos/resultados',
    children: CENTRAL_RIESGO_RESULTADO_ROUTES
  },

  // =============================
  // VECTOR DE COMPORTAMIENTO
  // ACTUAL
  // =============================
  {
    path: 'analisis/vector-comportamiento',
    children: VECTOR_COMPORTAMIENTO_ROUTES
  },

  // =============================
  // VECTOR DE COMPORTAMIENTO
  // POR CORTE
  // =============================
  {
    path: 'analisis/vector-comportamiento/corte',
    children: VECTOR_COMPORTAMIENTO_CORTE_ROUTES
  },

  // =============================
  // MATRIZ DE RODAMIENTO
  // =============================
  {
    path: 'analisis/matriz-rodamiento',
    children: MATRIZ_RODAMIENTO_ROUTES
  },

  // =============================
  // ANÁLISIS DE COSECHAS
  // =============================
  {
    path: 'analisis/cosechas',
    children: COSECHAS_ROUTES
  },

  // =============================
  // ANÁLISIS DE RIESGO Y DETERIORO
  // =============================
  {
    path: 'analisis/riesgo-deterioro',
    children: RIESGO_DETERIORO_ROUTES
  },

  // =============================
  // MORA TEMPRANA / CALIDAD DE ORIGINACIÓN
  // =============================
  {
    path: 'analisis/mora-temprana',
    children: MORA_TEMPRANA_ROUTES
  },

  // =============================
  // CURACIÓN Y REINCIDENCIA
  // =============================
  {
    path: 'analisis/curacion-reincidencia',
    children: CURACION_REINCIDENCIA_ROUTES
  },

  // =============================
  // CONCENTRACIÓN DE CARTERA
  // =============================
  {
    path: 'analisis/concentracion-cartera',
    children: CONCENTRACION_CARTERA_ROUTES
  },

  // =============================
  // GARANTÍAS Y COBERTURA
  // =============================
  {
    path: 'analisis/garantias-cobertura',
    children: GARANTIAS_COBERTURA_ROUTES
  },

  // =============================
  // RECIPROCIDAD DE APORTES
  // =============================
  {
    path: 'analisis/reciprocidad-aportes',
    children: RECIPROCIDAD_APORTES_ROUTES
  },

  // =============================
  // CANCELACIÓN Y PREPAGO
  // =============================
  {
    path: 'analisis/cancelacion-prepago',
    children: CANCELACION_PREPAGO_ROUTES
  },

  // =============================
  // ROLL FORWARD
  // =============================
  {
    path: 'analisis/roll-forward',
    children: ROLL_FORWARD_ROUTES
  }

];

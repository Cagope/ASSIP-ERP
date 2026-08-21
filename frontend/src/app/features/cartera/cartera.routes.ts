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
  }

];

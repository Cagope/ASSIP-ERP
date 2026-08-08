import {
  Routes
} from '@angular/router';

import {
  EvaluacionCarteraListComponent
} from './list/evaluacion-cartera-list.component';

import {
  EvaluacionCarteraGestionarComponent
} from './gestionar/evaluacion-cartera-gestionar.component';

import {
  EvaluacionCarteraResultadosComponent
} from './resultados/evaluacion-cartera-resultados.component';

import {
  EvaluacionResultadoDetalleComponent
} from './resultados/detalle/evaluacion-resultado-detalle.component';


export const EVALUACION_CARTERA_ROUTES: Routes = [

  // =========================================================
  // LISTADO DE EVALUACIONES
  // =========================================================

  {
    path: '',
    component: EvaluacionCarteraListComponent
  },

  // =========================================================
  // GESTIONAR EVALUACIÓN
  // =========================================================

  {
    path: ':idEvaluacionCartera/gestionar',
    component: EvaluacionCarteraGestionarComponent
  },

  // =========================================================
  // RESULTADOS DE LA EVALUACIÓN
  // =========================================================

  {
    path: ':idEvaluacionCartera/resultados',
    component: EvaluacionCarteraResultadosComponent
  },

  // =========================================================
  // DETALLE DE UN CRÉDITO EVALUADO
  // =========================================================

  {
    path:
      ':idEvaluacionCartera/resultados/:idEvaluacionCarteraCredito',
    component:
      EvaluacionResultadoDetalleComponent
  }

];

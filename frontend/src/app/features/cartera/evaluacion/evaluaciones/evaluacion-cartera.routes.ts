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

import {
  permisoGuard
} from '../../../../core/auth/permiso.guard';


export const EVALUACION_CARTERA_ROUTES: Routes = [

  // =========================================================
  // LISTADO DE EVALUACIONES
  // =========================================================

  {
    path: '',
    component: EvaluacionCarteraListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_EVALUACION_VIEW'
    }
  },

  // =========================================================
  // GESTIONAR EVALUACIÓN
  // =========================================================

  {
    path: ':idEvaluacionCartera/gestionar',
    component: EvaluacionCarteraGestionarComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_EVALUACION_VIEW'
    }
  },

  // =========================================================
  // RESULTADOS DE LA EVALUACIÓN
  // =========================================================

  {
    path: ':idEvaluacionCartera/resultados',
    component: EvaluacionCarteraResultadosComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_EVALUACION_VIEW'
    }
  },

  // =========================================================
  // DETALLE DE UN CRÉDITO EVALUADO
  // =========================================================

  {
    path:
      ':idEvaluacionCartera/resultados/:idEvaluacionCarteraCredito',
    component:
      EvaluacionResultadoDetalleComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_EVALUACION_VIEW'
    }
  }

];

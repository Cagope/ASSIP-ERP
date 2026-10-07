import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../../../core/auth/permiso.guard';

import {
  VectorComportamientoCorteComponent
} from './vector-comportamiento-corte.component';


export const VECTOR_COMPORTAMIENTO_CORTE_ROUTES: Routes = [

  {
    path: '',
    component:
      VectorComportamientoCorteComponent,
    canActivate: [
      permisoGuard
    ],
    data: {
      permiso:
        'CARTERA_ANALISIS_VIEW'
    }
  }

];

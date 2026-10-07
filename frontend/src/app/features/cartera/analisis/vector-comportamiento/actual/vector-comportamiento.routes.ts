import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../../../core/auth/permiso.guard';

import {
  VectorComportamientoComponent
} from './vector-comportamiento.component';


export const VECTOR_COMPORTAMIENTO_ROUTES: Routes = [

  {
    path: '',
    component:
      VectorComportamientoComponent,
    canActivate: [
      permisoGuard
    ],
    data: {
      permiso:
        'CARTERA_ANALISIS_VIEW'
    }
  }

];

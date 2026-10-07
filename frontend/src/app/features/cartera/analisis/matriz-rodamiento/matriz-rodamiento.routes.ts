import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../../core/auth/permiso.guard';

import {
  MatrizRodamientoComponent
} from './matriz-rodamiento.component';


export const MATRIZ_RODAMIENTO_ROUTES: Routes = [

  {
    path: '',
    component:
      MatrizRodamientoComponent,
    canActivate: [
      permisoGuard
    ],
    data: {
      permiso:
        'CARTERA_ANALISIS_VIEW'
    }
  }

];

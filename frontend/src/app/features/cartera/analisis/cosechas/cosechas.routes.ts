import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../../core/auth/permiso.guard';

import {
  CosechasComponent
} from './cosechas.component';


export const COSECHAS_ROUTES: Routes = [

  {
    path: '',
    component:
      CosechasComponent,
    canActivate: [
      permisoGuard
    ],
    data: {
      permiso:
        'CARTERA_ANALISIS_VIEW'
    }
  }

];

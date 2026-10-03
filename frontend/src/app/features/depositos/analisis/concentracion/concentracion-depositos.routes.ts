import {
  Routes
} from '@angular/router';

import {
  ConcentracionDepositosComponent
} from './concentracion-depositos.component';

import {
  permisoGuard
} from '../../../../core/auth/permiso.guard';


export const CONCENTRACION_DEPOSITOS_ROUTES: Routes = [

  {
    path: '',
    component: ConcentracionDepositosComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_ANALISIS_VIEW'
    }
  }

];

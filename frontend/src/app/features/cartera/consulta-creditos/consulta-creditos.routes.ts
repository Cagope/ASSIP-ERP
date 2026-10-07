import { Routes } from '@angular/router';

import {
  ConsultaCreditosComponent
} from './consulta-creditos.component';

import {
  permisoGuard
} from '../../../core/auth/permiso.guard';

export const CONSULTA_CREDITOS_ROUTES: Routes = [

  {
    path: '',
    component: ConsultaCreditosComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_VIEW'
    }
  }

];

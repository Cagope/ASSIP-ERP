import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../../core/auth/permiso.guard';

import {
  TasasCondicionesCdatComponent
} from './tasas-condiciones-cdat.component';


export const TASAS_CONDICIONES_CDAT_ROUTES: Routes = [

  {
    path: '',
    component: TasasCondicionesCdatComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_ANALISIS_VIEW'
    }
  }

];

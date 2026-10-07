import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';

import { ConcentracionCarteraComponent } from './concentracion-cartera.component';

export const CONCENTRACION_CARTERA_ROUTES: Routes = [
  {
    path: '',
    component: ConcentracionCarteraComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ANALISIS_VIEW'
    }
  }
];

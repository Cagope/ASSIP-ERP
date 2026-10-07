import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';
import { RiesgoDeterioroComponent } from './riesgo-deterioro.component';

export const RIESGO_DETERIORO_ROUTES: Routes = [
  {
    path: '',
    component: RiesgoDeterioroComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ANALISIS_VIEW'
    }
  }
];

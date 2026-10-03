import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';
import { ConcentracionCdatComponent } from './concentracion-cdat.component';

export const CONCENTRACION_CDAT_ROUTES: Routes = [
  {
    path: '',
    component: ConcentracionCdatComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_ANALISIS_VIEW'
    }
  }
];

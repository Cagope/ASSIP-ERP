import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';
import { CuracionReincidenciaComponent } from './curacion-reincidencia.component';

export const CURACION_REINCIDENCIA_ROUTES: Routes = [
  {
    path: '',
    component: CuracionReincidenciaComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ANALISIS_VIEW'
    }
  }
];

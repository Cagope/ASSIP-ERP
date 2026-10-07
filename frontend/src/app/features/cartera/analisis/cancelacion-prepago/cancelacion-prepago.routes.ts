import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';
import { CancelacionPrepagoComponent } from './cancelacion-prepago.component';

export const CANCELACION_PREPAGO_ROUTES: Routes = [
  {
    path: '',
    component: CancelacionPrepagoComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ANALISIS_VIEW'
    }
  }
];

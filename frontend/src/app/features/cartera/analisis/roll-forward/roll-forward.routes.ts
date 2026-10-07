import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';
import { RollForwardComponent } from './roll-forward.component';

export const ROLL_FORWARD_ROUTES: Routes = [
  {
    path: '',
    component: RollForwardComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ANALISIS_VIEW'
    }
  }
];

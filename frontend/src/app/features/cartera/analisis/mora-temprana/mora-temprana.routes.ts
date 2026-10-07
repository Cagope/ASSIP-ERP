import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';
import { MoraTempranaComponent } from './mora-temprana.component';

export const MORA_TEMPRANA_ROUTES: Routes = [
  {
    path: '',
    component: MoraTempranaComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ANALISIS_VIEW'
    }
  }
];

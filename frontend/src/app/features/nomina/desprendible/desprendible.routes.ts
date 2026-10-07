import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { DesprendibleComponent } from './desprendible.component';

export const DESPRENDIBLE_ROUTES: Routes = [
  {
    path: ':idPeriodo',
    component: DesprendibleComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_INFORMES_VIEW' }
  }
];

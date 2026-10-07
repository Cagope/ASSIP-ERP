import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';
import { AfpListComponent } from './afp-list.component';
import { AfpUpsertComponent } from './afp-upsert.component';

export const AFP_ROUTES: Routes = [
  {
    path: '',
    component: AfpListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },
  {
    path: 'nuevo',
    component: AfpUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  },
  {
    path: ':id/editar',
    component: AfpUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  }
];

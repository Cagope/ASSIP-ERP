import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { CajaCompensacionListComponent } from './caja-compensacion-list.component';
import { CajaCompensacionUpsertComponent } from './caja-compensacion-upsert.component';

export const CAJA_COMPENSACION_ROUTES: Routes = [
  {
    path: '',
    component: CajaCompensacionListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },
  {
    path: 'nuevo',
    component: CajaCompensacionUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  },
  {
    path: ':id/editar',
    component: CajaCompensacionUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  }
];

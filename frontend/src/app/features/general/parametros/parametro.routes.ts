import { Routes } from '@angular/router';
import { ParametroListComponent } from './parametro-list.component';
import { ParametroUpsertComponent } from './parametro-upsert.component';

import { permisoGuard } from '../../../core/auth/permiso.guard';

export const PARAMETROS_ROUTES: Routes = [
  { path: '', component: ParametroListComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_PARAMETROS_VIEW' } },
  { path: 'nuevo', component: ParametroUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_PARAMETROS_EDIT' } },
  { path: ':id/editar', component: ParametroUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_PARAMETROS_EDIT' } },
];

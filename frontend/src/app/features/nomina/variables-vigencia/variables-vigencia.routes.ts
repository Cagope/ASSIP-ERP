import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { VariablesVigenciaListComponent } from './variables-vigencia-list.component';
import { VariablesVigenciaUpsertComponent } from './variables-vigencia-upsert.component';

export const VARIABLES_VIGENCIA_ROUTES: Routes = [
  {
    path: '',
    component: VariablesVigenciaListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },
  {
    path: 'nuevo',
    component: VariablesVigenciaUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  },
  {
    path: ':id/editar',
    component: VariablesVigenciaUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  }
];

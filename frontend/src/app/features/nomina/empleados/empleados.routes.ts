import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { EmpleadosListComponent } from './empleados-list.component';
import { EmpleadosUpsertComponent } from './empleados-upsert.component';

export const EMPLEADOS_ROUTES: Routes = [
  {
    path: '',
    component: EmpleadosListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },
  {
    path: 'nuevo',
    component: EmpleadosUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_EMPLEADOS_EDIT' }
  },
  {
    path: ':id/editar',
    component: EmpleadosUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_EMPLEADOS_EDIT' }
  }
];

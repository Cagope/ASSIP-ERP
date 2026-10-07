import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { EmpleadoContratosListComponent } from './empleado-contratos-list.component';
import { EmpleadoContratosUpsertComponent } from './empleado-contratos-upsert.component';

export const EMPLEADO_CONTRATOS_ROUTES: Routes = [
  {
    path: '',
    component: EmpleadoContratosListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },
  {
    path: 'nuevo',
    component: EmpleadoContratosUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_EMPLEADOS_EDIT' }
  },
  {
    path: ':id/editar',
    component: EmpleadoContratosUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_EMPLEADOS_EDIT' }
  }
];

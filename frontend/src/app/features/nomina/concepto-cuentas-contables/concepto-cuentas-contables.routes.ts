import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { ConceptoCuentasContablesListComponent } from './concepto-cuentas-contables-list.component';
import { ConceptoCuentasContablesUpsertComponent } from './concepto-cuentas-contables-upsert.component';

export const CONCEPTO_CUENTAS_CONTABLES_ROUTES: Routes = [
  {
    path: '',
    component: ConceptoCuentasContablesListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },
  {
    path: 'nuevo',
    component: ConceptoCuentasContablesUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  },
  {
    path: ':idMapeo/editar',
    component: ConceptoCuentasContablesUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  }
];

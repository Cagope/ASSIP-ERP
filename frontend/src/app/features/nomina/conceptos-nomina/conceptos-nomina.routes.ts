import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { ConceptosNominaListComponent } from './conceptos-nomina-list.component';
import { ConceptosNominaUpsertComponent } from './conceptos-nomina-upsert.component';

export const CONCEPTOS_NOMINA_ROUTES: Routes = [
  {
    path: '',
    component: ConceptosNominaListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },
  {
    path: 'nuevo',
    component: ConceptosNominaUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  },
  {
    path: ':codigo/editar',
    component: ConceptosNominaUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  }
];

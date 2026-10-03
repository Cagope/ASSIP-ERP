import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { SarlaftListComponent } from './sarlaft-list.component';
import { SarlaftUpsertComponent } from './sarlaft-upsert.component';

/**
 * Rutas del submódulo SARLAFT
 *
 * Seguridad:
 * - Consulta: HOJAVIDA_SARLAFT_VIEW
 * - Creación: HOJAVIDA_SARLAFT_EDIT
 * - Consulta de registro existente: HOJAVIDA_SARLAFT_VIEW
 */
export const SARLAFT_ROUTES: Routes = [

  {
    path: '',
    component: SarlaftListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_SARLAFT_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: SarlaftUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_SARLAFT_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: SarlaftUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_SARLAFT_VIEW'
    }
  }

];

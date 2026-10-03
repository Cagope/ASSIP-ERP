import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../core/auth/permiso.guard';

import {
  CondicionesProteccionListComponent
} from './condiciones-proteccion-list.component';

import {
  CondicionesProteccionUpsertComponent
} from './condiciones-proteccion-upsert.component';

/**
 * Rutas del submódulo Condiciones de Protección
 *
 * Seguridad:
 * - Consulta: HOJAVIDA_PROTECCION_VIEW
 * - Creación: HOJAVIDA_PROTECCION_EDIT
 * - Consulta de registro existente: HOJAVIDA_PROTECCION_VIEW
 */
export const CONDICIONES_PROTECCION_ROUTES: Routes = [

  {
    path: '',
    component: CondicionesProteccionListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PROTECCION_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: CondicionesProteccionUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PROTECCION_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: CondicionesProteccionUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PROTECCION_VIEW'
    }
  }

];

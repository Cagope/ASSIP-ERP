import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../core/auth/permiso.guard';

import {
  PermisosEspecialesListComponent
} from './permisos-especiales-list.component';

import {
  PermisosEspecialesUpsertComponent
} from './permisos-especiales-upsert.component';

/**
 * Rutas del submódulo Permisos Especiales
 *
 * Seguridad:
 * - Consulta: HOJAVIDA_PROTECCION_VIEW
 * - Creación: HOJAVIDA_PROTECCION_EDIT
 * - Consulta de registro existente: HOJAVIDA_PROTECCION_VIEW
 */
export const PERMISOS_ESPECIALES_ROUTES: Routes = [

  {
    path: '',
    component: PermisosEspecialesListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PROTECCION_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: PermisosEspecialesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PROTECCION_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: PermisosEspecialesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PROTECCION_VIEW'
    }
  }

];

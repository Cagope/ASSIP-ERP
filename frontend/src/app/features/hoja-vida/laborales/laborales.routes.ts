import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { LaboralesListComponent } from './laborales-list.component';
import { LaboralesUpsertComponent } from './laborales-upsert.component';

/**
 * 💼 Rutas del módulo Laborales
 * ------------------------------------------------------------
 * Seguridad:
 *  - Listado              → HOJAVIDA_PERSONA_VIEW
 *  - Nuevo                → HOJAVIDA_PERSONA_EDIT
 *  - Gestionar existente  → HOJAVIDA_PERSONA_VIEW
 */
export const LABORALES_ROUTES: Routes = [

  {
    path: '',
    component: LaboralesListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: LaboralesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: LaboralesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  }

];

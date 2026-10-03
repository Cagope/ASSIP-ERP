import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { DatosFamiliaresListComponent } from './datos-familiares-list.component';
import { DatosFamiliaresUpsertComponent } from './datos-familiares-upsert.component';

/**
 * 👨‍👩‍👧‍👦 Rutas del módulo Datos Familiares
 * ------------------------------------------------------------
 * Seguridad:
 *  - Listado              → HOJAVIDA_PERSONA_VIEW
 *  - Nuevo                → HOJAVIDA_PERSONA_EDIT
 *  - Gestionar existente  → HOJAVIDA_PERSONA_VIEW
 */
export const DATOS_FAMILIARES_ROUTES: Routes = [

  {
    path: '',
    component: DatosFamiliaresListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: DatosFamiliaresUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: DatosFamiliaresUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  }

];

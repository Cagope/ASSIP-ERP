import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { FinancierosListComponent } from './financieros-list.component';
import { FinancierosUpsertComponent } from './financieros-upsert.component';

/**
 * 💰 Rutas del módulo Financieros
 * ------------------------------------------------------------
 * Define las rutas internas del submódulo Hoja de Vida → Financieros.
 *
 * Seguridad:
 *  - Listado / consulta  → HOJAVIDA_FINANCIERO_VIEW
 *  - Nuevo               → HOJAVIDA_FINANCIERO_EDIT
 *  - Gestionar existente → HOJAVIDA_FINANCIERO_VIEW
 */
export const FINANCIEROS_ROUTES: Routes = [

  {
    path: '',
    component: FinancierosListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_FINANCIERO_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: FinancierosUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_FINANCIERO_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: FinancierosUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_FINANCIERO_VIEW'
    }
  }

];

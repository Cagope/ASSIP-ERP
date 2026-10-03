import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { AperturaCuentasListComponent } from './apertura-cuentas-list.component';
import { AperturaCuentasUpsertComponent } from './apertura-cuentas-upsert.component';

/**
 * 💳 Rutas del módulo Apertura de Cuentas
 * ------------------------------------------------------------
 * Define las rutas internas del proceso Hoja de Vida → Apertura de Cuentas.
 *
 * Todo el proceso requiere el permiso:
 * HOJAVIDA_APERTURA_CUENTAS
 */
export const APERTURA_CUENTAS_ROUTES: Routes = [
  {
    path: '',
    component: AperturaCuentasListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_APERTURA_CUENTAS'
    }
  },
  {
    path: 'nuevo',
    component: AperturaCuentasUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_APERTURA_CUENTAS'
    }
  },
  {
    path: ':id/editar',
    component: AperturaCuentasUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_APERTURA_CUENTAS'
    }
  }
];

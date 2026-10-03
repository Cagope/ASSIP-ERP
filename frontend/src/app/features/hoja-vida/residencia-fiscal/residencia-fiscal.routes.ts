import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../core/auth/permiso.guard';

import {
  ResidenciaFiscalListComponent
} from './residencia-fiscal-list.component';

import {
  ResidenciaFiscalUpsertComponent
} from './residencia-fiscal-upsert.component';

/**
 * Rutas del submódulo Residencia Fiscal (FATCA / CRS)
 *
 * Seguridad:
 * - Consulta: HOJAVIDA_FISCAL_VIEW
 * - Creación: HOJAVIDA_FISCAL_EDIT
 * - Consulta de registro existente: HOJAVIDA_FISCAL_VIEW
 */
export const RESIDENCIA_FISCAL_ROUTES: Routes = [

  {
    path: '',
    component: ResidenciaFiscalListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_FISCAL_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: ResidenciaFiscalUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_FISCAL_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: ResidenciaFiscalUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_FISCAL_VIEW'
    }
  }

];

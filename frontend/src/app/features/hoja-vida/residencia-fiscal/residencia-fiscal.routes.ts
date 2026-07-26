import {
  Routes
} from '@angular/router';

import {
  ResidenciaFiscalListComponent
} from './residencia-fiscal-list.component';

import {
  ResidenciaFiscalUpsertComponent
} from './residencia-fiscal-upsert.component';

/**
 * 🌎 Rutas del submódulo Residencia Fiscal (FATCA / CRS)
 * ------------------------------------------------------------
 * Módulo: Hoja de Vida → Residencia Fiscal
 *
 * Rutas:
 *  - Listado principal
 *  - Creación
 *  - Edición
 */
export const RESIDENCIA_FISCAL_ROUTES: Routes = [

  {
    path: '',
    component: ResidenciaFiscalListComponent
  },

  {
    path: 'nuevo',
    component: ResidenciaFiscalUpsertComponent
  },

  {
    path: ':id/editar',
    component: ResidenciaFiscalUpsertComponent
  }

];

import { Routes } from '@angular/router';

import {
  CondicionesProteccionListComponent
} from './condiciones-proteccion-list.component';

import {
  CondicionesProteccionUpsertComponent
} from './condiciones-proteccion-upsert.component';

/**
 * 🛡️ Rutas del submódulo Condiciones de Protección
 * ------------------------------------------------------------
 * Módulo: Hoja de Vida → Condiciones de Protección
 *
 * Rutas:
 *  - Listado principal
 *  - Creación
 *  - Edición
 */
export const CONDICIONES_PROTECCION_ROUTES: Routes = [

  {
    path: '',
    component: CondicionesProteccionListComponent
  },

  {
    path: 'nuevo',
    component: CondicionesProteccionUpsertComponent
  },

  {
    path: ':id/editar',
    component: CondicionesProteccionUpsertComponent
  }

];

import { Routes } from '@angular/router';
import { SarlaftListComponent } from './sarlaft-list.component';
import { SarlaftUpsertComponent } from './sarlaft-upsert.component';

/**
 * 🧾 Rutas del submódulo SARLAFT
 * ------------------------------------------------------------
 * Módulo: Hoja de Vida → SARLAFT
 * Rutas:
 *  - Listado principal
 *  - Creación
 *  - Edición
 */
export const SARLAFT_ROUTES: Routes = [
  { path: '', component: SarlaftListComponent },
  { path: 'nuevo', component: SarlaftUpsertComponent },
  { path: ':id/editar', component: SarlaftUpsertComponent },
];

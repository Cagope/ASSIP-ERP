import { Routes } from '@angular/router';
import { LaboralesListComponent } from './laborales-list.component';
import { LaboralesUpsertComponent } from './laborales-upsert.component';

/**
 * 💼 Rutas del módulo Laborales
 * ------------------------------------------------------------
 * Define las rutas internas del submódulo Hoja de Vida → Laborales.
 *  - Listado principal
 *  - Formulario de creación
 *  - Formulario de edición
 */
export const LABORALES_ROUTES: Routes = [
  { path: '', component: LaboralesListComponent },
  { path: 'nuevo', component: LaboralesUpsertComponent },
  { path: ':id/editar', component: LaboralesUpsertComponent },
];

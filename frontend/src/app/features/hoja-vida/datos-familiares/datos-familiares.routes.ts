import { Routes } from '@angular/router';
import { DatosFamiliaresListComponent } from './datos-familiares-list.component';
import { DatosFamiliaresUpsertComponent } from './datos-familiares-upsert.component';

/**
 * 👨‍👩‍👧‍👦 Rutas del módulo Datos Familiares
 * ------------------------------------------------------------
 * Define las rutas internas del submódulo Hoja de Vida → Datos Familiares.
 *  - Listado principal
 *  - Formulario de creación
 *  - Formulario de edición
 */
export const DATOS_FAMILIARES_ROUTES: Routes = [
  { path: '', component: DatosFamiliaresListComponent },
  { path: 'nuevo', component: DatosFamiliaresUpsertComponent },
  { path: ':id/editar', component: DatosFamiliaresUpsertComponent },
];

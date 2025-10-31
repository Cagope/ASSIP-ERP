import { Routes } from '@angular/router';
import { FinancierosListComponent } from './financieros-list.component';
import { FinancierosUpsertComponent } from './financieros-upsert.component';

/**
 * 💰 Rutas del módulo Financieros
 * ------------------------------------------------------------
 * Define las rutas internas del submódulo Hoja de Vida → Financieros.
 *  - Listado principal
 *  - Formulario de creación
 *  - Formulario de edición
 */
export const FINANCIEROS_ROUTES: Routes = [
  { path: '', component: FinancierosListComponent },
  { path: 'nuevo', component: FinancierosUpsertComponent },
  { path: ':id/editar', component: FinancierosUpsertComponent },
];

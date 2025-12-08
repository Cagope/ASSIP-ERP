import { Routes } from '@angular/router';
import { AperturaCuentasListComponent } from './apertura-cuentas-list.component';
import { AperturaCuentasUpsertComponent } from './apertura-cuentas-upsert.component';

/**
 * 💳 Rutas del módulo Apertura de Cuentas
 * ------------------------------------------------------------
 * Define las rutas internas del proceso Hoja de Vida → Apertura de Cuentas.
 *  - Listado principal de asociados con sus cuentas
 *  - Formulario para nueva cuenta
 *  - Formulario para edición / gestión
 */
export const APERTURA_CUENTAS_ROUTES: Routes = [
  { path: '', component: AperturaCuentasListComponent },
  { path: 'nuevo', component: AperturaCuentasUpsertComponent },
  { path: ':id/editar', component: AperturaCuentasUpsertComponent },
];

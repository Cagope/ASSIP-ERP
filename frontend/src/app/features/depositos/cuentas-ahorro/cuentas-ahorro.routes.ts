import { Routes } from '@angular/router';
import { CuentasAhorroListComponent } from './cuentas-ahorro-list.component';

/**
 * 💰 Rutas — Submódulo Cuentas de Ahorro
 * ------------------------------------------------------------
 * - Listado principal de cuentas (con filtros por persona)
 * - En el futuro: vista detalle, movimientos y extractos
 */
export const CUENTAS_AHORRO_ROUTES: Routes = [
  { path: '', component: CuentasAhorroListComponent },
  // { path: ':id/detalle', component: CuentasAhorroDetalleComponent }, // 🔜 futuro
];

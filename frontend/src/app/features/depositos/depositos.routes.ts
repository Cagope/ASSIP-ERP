import { Routes } from '@angular/router';

// 💰 Submódulo: Cuentas de Ahorro
import { CUENTAS_AHORRO_ROUTES } from './cuentas-ahorro/cuentas-ahorro.routes';

// 📊 Submódulo: Informes — Saldos a fecha de corte
import { SALDOS_CORTE_ROUTES } from './informes/saldos-corte/saldos-corte.routes';

/**
 * 🏦 Rutas principales — Módulo Depósitos
 * ------------------------------------------------------------
 * Estructura base de navegación interna del módulo:
 *  - Cuentas de ahorro
 *  - Informes (incluye: Saldos a corte)
 */
export const DEPOSITOS_ROUTES: Routes = [
  // 💰 Cuentas de ahorro
  { path: 'cuentas-ahorro', children: CUENTAS_AHORRO_ROUTES },

  // 📊 Informes — Saldos a fecha de corte
  { path: 'informes/saldos-corte', children: SALDOS_CORTE_ROUTES },
];

import { Routes } from '@angular/router';

// 💰 Submódulo: Cuentas de Ahorro
import { CUENTAS_AHORRO_ROUTES } from './cuentas-ahorro/cuentas-ahorro.routes';

/**
 * 🏦 Rutas principales — Módulo Depósitos
 * ------------------------------------------------------------
 * Estructura base de navegación interna del módulo:
 *  - Cuentas de ahorro (listado y detalle)
 *  - Próximamente: movimientos, extractos, intereses
 */
export const DEPOSITOS_ROUTES: Routes = [
  // 💰 Cuentas de ahorro
  { path: 'cuentas-ahorro', children: CUENTAS_AHORRO_ROUTES },

  // 📊 Movimientos y extractos (pendiente)
  // { path: 'movimientos', children: MOVIMIENTOS_ROUTES },

  // 🧮 Liquidación de intereses (pendiente)
  // { path: 'liquidacion', children: LIQUIDACION_ROUTES },
];

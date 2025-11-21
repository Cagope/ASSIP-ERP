import { Routes } from '@angular/router';

// 💰 Submódulo: Cuentas de Ahorro
import { CUENTAS_AHORRO_ROUTES } from './cuentas-ahorro/cuentas-ahorro.routes';

// 🏛 Submódulo: Formas de Ahorro
import { FORMAS_AHORRO_ROUTES } from './formas-ahorro/formas-ahorro.routes';

// 📊 Submódulo: Informes — Saldos a fecha de corte
import { SALDOS_CORTE_ROUTES } from './informes/saldos-corte/saldos-corte.routes';

// 📘 Submódulo: Informes — Cuentas Nuevas o Retiradas
import { cuentasNRRoutes } from './informes/cuentas-nr/cuentas-nr.routes';

// 🧩 NUEVO — Informe por Rangos (Saldos / Edad / Antigüedad)
import { RANGOS_ROUTES } from './informes/rangos/rangos.routes';

// ⚠️ NUEVO — Informe de Inconsistencias de Saldos
import { INCONSISTENCIAS_ROUTES } from './informes/inconsistencias/inconsistencias.routes';

/**
 * 🏦 Rutas principales — Módulo Depósitos
 * ------------------------------------------------------------
 * Estructura base de navegación interna del módulo:
 *  - Cuentas de ahorro
 *  - Formas de ahorro
 *  - Informes (Saldos a corte, Cuentas NR, Rangos)
 */
export const DEPOSITOS_ROUTES: Routes = [

  // 💰 Cuentas de ahorro
  { path: 'cuentas-ahorro', children: CUENTAS_AHORRO_ROUTES },

  // 🏛 Formas de Ahorro
  { path: 'formas-ahorro', children: FORMAS_AHORRO_ROUTES },

  // 📊 Informes — Saldos a fecha de corte
  { path: 'informes/saldos-corte', children: SALDOS_CORTE_ROUTES },

  // 🆕 Informes — Cuentas Nuevas o Retiradas
  { path: 'informes/cuentas-nr', children: cuentasNRRoutes },

  // 🧩 🔥 NUEVO INFORME — Rangos
  { path: 'informes/rangos', children: RANGOS_ROUTES },

  // ⚠️ NUEVO — Informe: Inconsistencias de saldos
  { path: 'informes/inconsistencias', children: INCONSISTENCIAS_ROUTES },


];

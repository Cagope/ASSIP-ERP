import { Routes } from '@angular/router';

// 💰 Submódulo: Cuentas de Ahorro
import { CUENTAS_AHORRO_ROUTES } from './cuentas-ahorro/cuentas-ahorro.routes';

// 🏛 Submódulo: Formas de Ahorro
import { FORMAS_AHORRO_ROUTES } from './formas-ahorro/formas-ahorro.routes';

// 📊 Submódulo: Informes — Saldos a fecha de corte
import { SALDOS_CORTE_ROUTES } from './informes/saldos-corte/saldos-corte.routes';

// 📘 Submódulo: Informes — Cuentas Nuevas o Retiradas
import { cuentasNRRoutes } from './informes/cuentas-nr/cuentas-nr.routes';

// 🧩 Submódulo: Informes — Rangos (Saldos / Edad / Antigüedad)
import { RANGOS_ROUTES } from './informes/rangos/rangos.routes';

// ⚠️ Submódulo: Informes — Inconsistencias de Saldos
import { INCONSISTENCIAS_ROUTES } from './informes/inconsistencias/inconsistencias.routes';


/**
 * 🏦 Rutas principales — Módulo Depósitos
 * ------------------------------------------------------------
 * Estructura base:
 *  - Cuentas de ahorro
 *  - Formas de ahorro
 *  - Informes (varios)
 *  - Procesos (Revalorización)
 */
export const DEPOSITOS_ROUTES: Routes = [

  // 💰 Cuentas de ahorro
  { path: 'cuentas-ahorro', children: CUENTAS_AHORRO_ROUTES },

  // 🏛 Formas de Ahorro
  { path: 'formas-ahorro', children: FORMAS_AHORRO_ROUTES },

  // 📊 Informes — Saldos a fecha de corte
  { path: 'informes/saldos-corte', children: SALDOS_CORTE_ROUTES },

  // 📘 Informes — Cuentas Nuevas o Retiradas
  { path: 'informes/cuentas-nr', children: cuentasNRRoutes },

  // 🧩 Informes — Rangos
  { path: 'informes/rangos', children: RANGOS_ROUTES },

  // ⚠️ Informes — Inconsistencias
  { path: 'informes/inconsistencias', children: INCONSISTENCIAS_ROUTES },

  // ⚙️ Procesos — Revalorización de Aportes
  {
    path: 'procesos/revalorizacion',
    loadChildren: () =>
      import('./procesos/revalorizacion/revalorizacion.routes')
        .then(m => m.revalorizacionRoutes)
  },

  // ⚙️ Procesos — interes diario sobre saldo minimo

  {
    path: 'procesos/interes-diario-sm',
    loadChildren: () =>
      import('./procesos/interes-diario-sm/interes-diario-sm.routes')
        .then(m => m.interesDiarioSmRoutes)
  }

];

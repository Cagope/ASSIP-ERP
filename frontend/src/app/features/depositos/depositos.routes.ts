import { Routes } from '@angular/router';

// 🟦 Consulta Cuentas de Ahorro (ANTES cuentas-ahorro)
import { CONSULTA_CUENTAS_AHORRO_ROUTES }
  from './informes/consulta-cuentas-ahorro/consulta-cuentas-ahorro.routes';

// 🏛 Submódulo: Formas de Ahorro
import { FORMAS_AHORRO_ROUTES } from './formas-ahorro/formas-ahorro.routes';

// 📊 Informes — Saldos a fecha de corte
import { SALDOS_CORTE_ROUTES } from './informes/saldos-corte/saldos-corte.routes';

// 📘 Informes — Cuentas Nuevas o Retiradas
import { cuentasNRRoutes } from './informes/cuentas-nr/cuentas-nr.routes';

// 🧩 Informes — Rangos
import { RANGOS_ROUTES } from './informes/rangos/rangos.routes';

// ⚠️ Informes — Inconsistencias
import { INCONSISTENCIAS_ROUTES } from './informes/inconsistencias/inconsistencias.routes';

// ⭐ Proceso — Habilidad del Asociado
import { habilidadAsociadoRoutes }
  from './procesos/habilidad-asociado/habilidad-asociado.routes';

// ⭐ Proceso — Crud cuentas de ahorro
import { CUENTAS_AHORRO_ROUTES }
  from './cuentas-ahorro/cuentas-ahorro.routes';


export const DEPOSITOS_ROUTES: Routes = [

  // 🟦 CONSULTA CUENTAS DE AHORRO

  {
    path: 'cuentas-ahorro',
    children: CUENTAS_AHORRO_ROUTES
  },

  {
    path: 'informes/consulta-cuentas-ahorro',
    children: CONSULTA_CUENTAS_AHORRO_ROUTES
  },

  // 🏛 Formas de ahorro
  {
    path: 'formas-ahorro',
    children: FORMAS_AHORRO_ROUTES
  },

  // 📊 Informes — Saldos Corte
  {
    path: 'informes/saldos-corte',
    children: SALDOS_CORTE_ROUTES
  },

  // 📘 Informes — Cuentas nuevas/retiradas
  {
    path: 'informes/cuentas-nr',
    children: cuentasNRRoutes
  },

  // ⭐ Habilidad del Asociado
  {
    path: 'procesos/habilidad-asociado',
    children: habilidadAsociadoRoutes
  },

  // 🧩 Informes — Rangos
  {
    path: 'informes/rangos',
    children: RANGOS_ROUTES
  },

  // ⚠️ Informes — Inconsistencias
  {
    path: 'informes/inconsistencias',
    children: INCONSISTENCIAS_ROUTES
  },

  // ⚙️ Revalorización
  {
    path: 'procesos/revalorizacion',
    loadChildren: () =>
      import('./procesos/revalorizacion/revalorizacion.routes')
        .then(m => m.revalorizacionRoutes)
  },

  // ⚙️ Interés Diario SM
  {
    path: 'procesos/interes-diario-sm',
    loadChildren: () =>
      import('./procesos/interes-diario-sm/interes-diario-sm.routes')
        .then(m => m.interesDiarioSmRoutes)
  },

  // ⚙️ Interés Mensual SM
  {
    path: 'procesos/interes-mensual-sm',
    loadChildren: () =>
      import('./procesos/interes-mensual-sm/interes-mensual-sm.routes')
        .then(m => m.interesMensualSmRoutes)
  },

  // ⚙️ Interés Mensual TAC
  {
    path: 'procesos/interes-mensual-tac',
    loadChildren: () =>
      import('./procesos/interes-mensual-tac/interes-mensual-tac.routes')
        .then(m => m.interesMensualTacRoutes)
  }

];

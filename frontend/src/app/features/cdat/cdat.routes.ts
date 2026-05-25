import { Routes } from '@angular/router';

// =============================
// CDAT CRUD
// =============================
import { CDATS_ROUTES } from './cdats/cdats.routes';

// =============================
// CONSULTAS
// =============================
import { CONSULTA_CDATS_ROUTES } from './informes/consulta-cdats/consulta-cdats.routes';

// =============================
// CIERRE MENSUAL CDAT
// =============================
import { CIERRE_MENSUAL_CDAT_ROUTES } from './cierre_mensual_cdat/cierre-mensual-cdat.routes';

// =============================
// CAUSACIÓN MENSUAL CDAT
// =============================
import { CAUSACION_MENSUAL_CDAT_ROUTES } from './causacion_mensual_cdat/causacion-mensual-cdat.routes';

// =============================
// ESTADÍSTICOS CDAT
// =============================
import { ESTADISTICOS_CDAT_ROUTES } from './informes/estadisticos-cdat/estadisticos-cdat.routes';

import { FECHAS_CDAT_ROUTES } from './informes/fechas-cdat/fechas-cdat.routes';


export const CDAT_ROUTES: Routes = [

  // =============================
  // INCLUSIÓN / APERTURA
  // =============================
  {
    path: 'cdats',
    children: CDATS_ROUTES
  },

  // =============================
  // CANCELACIÓN / RENOVACIÓN
  // =============================
  {
    path: 'cancelacion',
    loadComponent: () =>
      import('./cancelacion/cdat-cancelacion.component')
        .then(m => m.CdatCancelacionComponent),
  },

  // =============================
  // LIQUIDACIÓN DIARIA
  // =============================
  {
    path: 'liquidacion-diaria',
    loadComponent: () =>
      import('./liquidacion-diaria/cdat-liquidacion-diaria.component')
        .then(m => m.CdatLiquidacionDiariaComponent),
  },

  // =============================
  // CIERRE MENSUAL CDAT
  // =============================
  {
    path: 'cierre-mensual-cdat',
    children: CIERRE_MENSUAL_CDAT_ROUTES
  },

  // =============================
  // CAUSACIÓN MENSUAL CDAT
  // =============================
  {
    path: 'causacion-mensual-cdat',
    children: CAUSACION_MENSUAL_CDAT_ROUTES
  },

  // =============================
  // CONSULTA CDATS
  // =============================
  {
    path: 'informes/consulta-cdats',
    children: CONSULTA_CDATS_ROUTES
  },

  // =============================
  // SIMULADOR CDAT
  // =============================
  {
    path: 'informes/simulador-cdat',
    loadChildren: () =>
      import('./informes/simulador-cdat/simulador-cdat.routes')
        .then(m => m.SIMULADOR_CDAT_ROUTES),
  },

  // =============================
  // ESTADISTICOS CDAT
  // =============================
  {
    path: 'informes/estadisticos-cdat',
    children: ESTADISTICOS_CDAT_ROUTES
  },

  {
    path: 'informes/fechas-cdat',
    children: FECHAS_CDAT_ROUTES
  }

];

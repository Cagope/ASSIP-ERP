import { Routes } from '@angular/router';
import { permisoGuard } from '../../core/auth/permiso.guard';

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

// =============================
// FECHAS CDAT
// =============================
import { FECHAS_CDAT_ROUTES } from './informes/fechas-cdat/fechas-cdat.routes';

// =============================
// ANÁLISIS - CONCENTRACIÓN CDAT
// =============================
import { CONCENTRACION_CDAT_ROUTES } from './analisis/concentracion/concentracion-cdat.routes';

// =============================
// ANÁLISIS - TASAS Y CONDICIONES CDAT
// =============================
import { TASAS_CONDICIONES_CDAT_ROUTES } from './analisis/tasas-condiciones/tasas-condiciones-cdat.routes';


export const CDAT_ROUTES: Routes = [

  // =============================
  // INCLUSIÓN / APERTURA
  // =============================
  {
    path: 'cdats',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_APERTURA_CREATE' },
    children: CDATS_ROUTES
  },

  // =============================
  // CANCELACIÓN / RENOVACIÓN
  // =============================
  {
    path: 'cancelacion',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_CANCELACION_EXECUTE' },
    loadComponent: () =>
      import('./cancelacion/cdat-cancelacion.component')
        .then(m => m.CdatCancelacionComponent)
  },

  // =============================
  // LIQUIDACIÓN DIARIA
  // =============================
  {
    path: 'liquidacion-diaria',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_LIQUIDACION_DIARIA_EXECUTE' },
    loadComponent: () =>
      import('./liquidacion-diaria/cdat-liquidacion-diaria.component')
        .then(m => m.CdatLiquidacionDiariaComponent)
  },

  // =============================
  // CIERRE MENSUAL CDAT
  // =============================
  {
    path: 'cierre-mensual-cdat',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_CIERRE_MENSUAL_EXECUTE' },
    children: CIERRE_MENSUAL_CDAT_ROUTES
  },

  // =============================
  // CAUSACIÓN MENSUAL CDAT
  // =============================
  {
    path: 'causacion-mensual-cdat',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_CAUSACION_MENSUAL_EXECUTE' },
    children: CAUSACION_MENSUAL_CDAT_ROUTES
  },

  // =============================
  // CONSULTA CDATS
  // =============================
  {
    path: 'informes/consulta-cdats',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_INFORMES_VIEW' },
    children: CONSULTA_CDATS_ROUTES
  },

  // =============================
  // SIMULADOR CDAT
  // =============================
  {
    path: 'informes/simulador-cdat',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_INFORMES_VIEW' },
    loadChildren: () =>
      import('./informes/simulador-cdat/simulador-cdat.routes')
        .then(m => m.SIMULADOR_CDAT_ROUTES)
  },

  // =============================
  // ESTADÍSTICOS CDAT
  // =============================
  {
    path: 'informes/estadisticos-cdat',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_INFORMES_VIEW' },
    children: ESTADISTICOS_CDAT_ROUTES
  },

  // =============================
  // FECHAS CDAT
  // =============================
  {
    path: 'informes/fechas-cdat',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_INFORMES_VIEW' },
    children: FECHAS_CDAT_ROUTES
  },

  // =============================
  // ANÁLISIS - CONCENTRACIÓN CDAT
  // =============================
  {
    path: 'analisis/concentracion',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_ANALISIS_VIEW' },
    children: CONCENTRACION_CDAT_ROUTES
  },

  // =============================
  // ANÁLISIS - TASAS Y CONDICIONES CDAT
  // =============================
  {
    path: 'analisis/tasas-condiciones',
    canActivate: [permisoGuard],
    data: { permiso: 'CDAT_ANALISIS_VIEW' },
    children: TASAS_CONDICIONES_CDAT_ROUTES
  }

];

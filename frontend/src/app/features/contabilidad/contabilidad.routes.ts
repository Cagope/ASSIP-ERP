
import { Routes } from '@angular/router';

import { permisoGuard } from '../../core/auth/permiso.guard';

import { PLAN_CUENTAS_ROUTES } from './plan-cuentas/plan-cuentas.routes';
import { TIPOS_COMPROBANTES_ROUTES } from './tipos-comprobantes/tipos-comprobantes.routes';

/**
 * Rutas del módulo de Contabilidad.
 *
 * Protege el acceso a cada funcionalidad mediante
 * los permisos efectivos de la sesión.
 */
export const CONTABILIDAD_ROUTES: Routes = [

  // =========================================================
  // OPERACIÓN - COMPROBANTES
  // =========================================================

  {
    path: 'comprobantes',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_NEW'
    },
    loadComponent: () =>
      import(
        './comprobantes/crear/comprobantes-crear.component'
      ).then(
        m => m.ComprobantesCrearComponent
      )
  },

  // =========================================================
  // PARAMETRIZACIÓN - PLAN DE CUENTAS
  // =========================================================

  {
    path: 'plan-cuentas',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_VIEW'
    },
    children: PLAN_CUENTAS_ROUTES
  },

  // =========================================================
  // PARAMETRIZACIÓN - TIPOS DE COMPROBANTES
  // =========================================================

  {
    path: 'tipos-comprobantes',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_VIEW'
    },
    children: TIPOS_COMPROBANTES_ROUTES
  }

];

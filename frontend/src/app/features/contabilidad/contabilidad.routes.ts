
import { Routes } from '@angular/router';
import { permisoGuard } from '../../core/auth/permiso.guard';
import { PLAN_CUENTAS_ROUTES } from './plan-cuentas/plan-cuentas.routes';
import { TIPOS_COMPROBANTES_ROUTES } from './tipos-comprobantes/tipos-comprobantes.routes';
import { INFORMES_ESPECIALES_ROUTES } from './informes/especiales/informes-especiales.routes';

/**
 * Rutas del módulo de Contabilidad.
 * Protege cada funcionalidad según el permiso efectivo del usuario.
 */
export const CONTABILIDAD_ROUTES: Routes = [
  // =========================================================
  // OPERACIÓN - COMPROBANTES
  // =========================================================
  {
    path: 'comprobantes',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_NEW' },
    loadComponent: () =>
      import('./comprobantes/crear/comprobantes-crear.component')
        .then(m => m.ComprobantesCrearComponent)
  },
  {
    path: 'comprobantes/consultar',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_VIEW' },
    loadComponent: () =>
      import('./comprobantes/consulta/comprobantes-consulta.component')
        .then(m => m.ComprobantesConsultaComponent)
  },
  {
    path: 'comprobantes/editar',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_EDIT' },
    loadComponent: () =>
      import('./comprobantes/editar/comprobantes-editar.component')
        .then(m => m.ComprobantesEditarComponent)
  },
  {
    path: 'comprobantes/copiar',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_COPY' },
    loadComponent: () =>
      import('./comprobantes/copiar/comprobantes-copiar.component')
        .then(m => m.ComprobantesCopiarComponent)
  },
  {
    path: 'comprobantes/importar',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_NEW' },
    loadComponent: () =>
      import('./comprobantes/importar/comprobantes-importar.component')
        .then(m => m.ComprobantesImportarComponent)
  },

  // =========================================================
  // INFORMES - AUXILIARES CONTABLES
  // =========================================================
  {
    path: 'informes/auxiliares/cuentas',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_AUXILIAR_CUENTAS_VIEW' },
    loadComponent: () =>
      import('./informes/auxiliares/auxiliar-cuentas/auxiliar-cuentas.component')
        .then(m => m.AuxiliarCuentasComponent)
  },

  // =========================================================
  // INFORMES - INFORMES ESPECIALES
  // =========================================================
  {
    path: 'informes/especiales',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW'
    },
    children: INFORMES_ESPECIALES_ROUTES
  },

  // =========================================================
  // PARAMETRIZACIÓN - PLAN DE CUENTAS
  // =========================================================
  {
    path: 'plan-cuentas',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_VIEW' },
    children: PLAN_CUENTAS_ROUTES
  },

  // =========================================================
  // PARAMETRIZACIÓN - TIPOS DE COMPROBANTES
  // =========================================================
  {
    path: 'tipos-comprobantes',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_VIEW' },
    children: TIPOS_COMPROBANTES_ROUTES
  }
];

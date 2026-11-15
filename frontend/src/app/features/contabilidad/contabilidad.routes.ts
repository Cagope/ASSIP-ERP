
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
  // OPERACIÓN - CIERRE DIARIO
  // =========================================================
  {
    path: 'cierres/diario',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_PERIODOS_VIEW' },
    loadComponent: () =>
      import('./cierres/cierre-diario/cierre-diario.component')
        .then(m => m.CierreDiarioComponent)
  },

  // =========================================================
  // OPERACIÓN - CIERRE MENSUAL CONTABLE
  // =========================================================
  {
    path: 'cierres/mensual',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_PERIODOS_VIEW' },
    loadComponent: () =>
      import('./cierres/cierre-mensual/cierre-mensual.component')
        .then(m => m.CierreMensualComponent)
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

  {
    path: 'informes/auxiliares/exportacion',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_AUXILIAR_CUENTAS_VIEW' },
    loadComponent: () =>
      import('./informes/auxiliares/auxiliar-exportacion/auxiliar-exportacion.component')
        .then(m => m.AuxiliarExportacionComponent)
  },

  // =========================================================
  // INFORMES - BALANCE DETALLADO MENSUAL
  // =========================================================
  {
    path: 'informes/balances',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
    },
    loadComponent: () =>
      import('./informes/balances/balance-detallado-mensual/balance-detallado-mensual.component')
        .then(m => m.BalanceDetalladoMensualComponent)
  },

  // =========================================================
  // INFORMES - BALANCE COMPARADO MENSUAL
  // =========================================================
  {
    path: 'informes/balances/comparado-mensual',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
    },
    loadComponent: () =>
      import('./informes/balances/balance_comparado_mensual/balance-comparado-mensual.component')
        .then(m => m.BalanceComparadoMensualComponent)
  },

  // =========================================================
  // INFORMES - BALANCE DE COMPROBACIÓN
  // =========================================================
  {
    path: 'informes/balances/comprobacion',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
    },
    loadComponent: () =>
      import('./informes/balances/balance_comprobacion/balance-comprobacion.component')
        .then(m => m.BalanceComprobacionComponent)
  },

  // =========================================================
  // INFORMES - ESTADO DE CUENTAS
  // =========================================================
  {
    path: 'informes/balances/estado-cuentas',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
    },
    loadComponent: () =>
      import('./informes/balances/estado_cuentas/estado-cuentas.component')
        .then(m => m.EstadoCuentasComponent)
  },

  // =========================================================
  // INFORMES - BALANCES POR VIGENCIAS
  // =========================================================
  {
    path: 'informes/balances/vigencias',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
    },
    loadComponent: () =>
      import('./informes/balances/balances_vigencias/balances-vigencias.component')
        .then(m => m.BalancesVigenciasComponent)
  },

  // =========================================================
  // INFORMES - EXCEDENTES O PÉRDIDAS SIN FORMATO (PyG)
  // =========================================================
  {
    path: 'informes/pyg/sin-formato',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_EXCEDENTES_PERDIDAS_VIEW'
    },
    loadComponent: () =>
      import('./informes/pyg/sin-formato/pyg-sin-formato.component')
        .then(m => m.PyGSinFormatoComponent)
  },

  // =========================================================
  // INFORMES - EXCEDENTES O PÉRDIDAS CON FORMATO COMPARADO (PyG)
  // =========================================================
  {
    path: 'informes/pyg/comparado',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_EXCEDENTES_PERDIDAS_VIEW'
    },
    loadComponent: () =>
      import('./informes/pyg/comparado/pyg-comparado.component')
        .then(m => m.PyGComparadoComponent)
  },

  // =========================================================
  // INFORMES - EXCEDENTES O PÉRDIDAS CON FORMATO INDIVIDUAL (PyG)
  // =========================================================
  {
    path: 'informes/pyg/individual',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_EXCEDENTES_PERDIDAS_VIEW'
    },
    loadComponent: () =>
      import('./informes/pyg/individual/pyg-individual.component')
        .then(m => m.PyGIndividualComponent)
  },

  // =========================================================
  // INFORMES - OFICIALES - CAJA DIARIO
  // =========================================================
  {
    path: 'informes/oficiales/caja-diario',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_INFORMES_OFICIALES_VIEW'
    },
    loadComponent: () =>
      import('./informes/oficiales/caja-diario/caja-diario.component')
        .then(m => m.CajaDiarioComponent)
  },

  // =========================================================
  // INFORMES - OFICIALES - MAYOR Y BALANCE
  // =========================================================
  {
    path: 'informes/oficiales/mayor-balance',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CONTABILIDAD_INFORMES_OFICIALES_VIEW'
    },
    loadComponent: () =>
      import('./informes/oficiales/mayor_balance/mayor-balance.component')
        .then(m => m.MayorBalanceComponent)
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

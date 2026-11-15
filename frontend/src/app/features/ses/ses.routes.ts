import { Routes } from '@angular/router';
import { permisoGuard } from '../../core/auth/permiso.guard';

export const sesRoutes: Routes = [

  // =========================================================
  // FORMATO 9999 - ASOCIADOS
  // =========================================================
  {
    path: 'asociados',
    canActivate: [permisoGuard],
    data: {
      permiso: 'SES_FORMATOS_VIEW'
    },
    loadComponent: () =>
      import('./formato9999/formato9999.component')
        .then(m => m.Formato9999Component)
  },

  // =========================================================
  // FORMATO 9013 - APORTES SOCIALES
  // =========================================================
  {
    path: 'aportes',
    canActivate: [permisoGuard],
    data: {
      permiso: 'SES_FORMATOS_VIEW'
    },
    loadComponent: () =>
      import('./formato9013/formato9013.component')
        .then(m => m.Formato9013Component)
  },

  // =========================================================
  // FORMATO 9026 - AHORROS
  // Depósitos y CDAT - Cierres mensuales históricos
  // =========================================================
  {
    path: 'ahorros',
    canActivate: [permisoGuard],
    data: {
      permiso: 'SES_FORMATOS_VIEW'
    },
    loadComponent: () =>
      import('./formato9026/formato9026.component')
        .then(m => m.Formato9026Component)
  },

  // =========================================================
  // FORMATO 9027 - CARTERA INDIVIDUAL
  // =========================================================
  {
    path: 'cartera',
    canActivate: [permisoGuard],
    data: {
      permiso: 'SES_FORMATOS_VIEW'
    },
    loadComponent: () =>
      import('./formato9027/formato9027.component')
        .then(m => m.Formato9027Component)
  },

  // =========================================================
  // FORMATO 3 - CONTABILIDAD
  // =========================================================
  {
    path: 'contabilidad/formato-3',
    canActivate: [permisoGuard],
    data: {
      permiso: 'SES_FORMATOS_VIEW'
    },
    loadComponent: () =>
      import('./formato3/formato3.component')
        .then(m => m.Formato3Component)
  }

];

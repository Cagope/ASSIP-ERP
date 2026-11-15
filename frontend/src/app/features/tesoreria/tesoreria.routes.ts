
import { Routes } from '@angular/router';
import { permisoGuard } from '../../core/auth/permiso.guard';

export const TESORERIA_ROUTES: Routes = [

  // =========================================================
  // MOVIMIENTOS DE FONDOS
  // Consignaciones, retiros y traslados
  // =========================================================
  {
    path: 'movimientos-fondos',
    canActivate: [permisoGuard],
    data: { permiso: 'TESORERIA_MOVIMIENTOS_FONDOS_VIEW' },
    loadComponent: () =>
      import('./movimientos-fondos/movimientos-fondos')
        .then(m => m.MovimientosFondosComponent)
  },

  // =========================================================
  // OPERACIONES PENDIENTES
  // Obligaciones, distribución y ejecución de pagos
  // =========================================================
  {
    path: 'operaciones-pendientes',
    canActivate: [permisoGuard],
    data: { permiso: 'TESORERIA_OPERACIONES_PENDIENTES_VIEW' },
    loadComponent: () =>
      import('./operaciones-pendientes/operaciones-pendientes.component')
        .then(m => m.OperacionesPendientesComponent)
  },

  // =========================================================
  // AUTORIZACIÓN DE COMPROBANTES
  // Consulta, autorización, rechazo y anulación
  // =========================================================
  {
    path: 'autorizacion-comprobantes',
    canActivate: [permisoGuard],
    data: { permiso: 'TESORERIA_AUTORIZACION_COMPROBANTES_VIEW' },
    loadComponent: () =>
      import('./autorizacion-comprobantes/autorizacion-comprobantes')
        .then(m => m.AutorizacionComprobantesComponent)
  }

];

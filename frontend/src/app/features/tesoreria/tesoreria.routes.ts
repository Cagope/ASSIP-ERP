
import { Routes } from '@angular/router';

export const TESORERIA_ROUTES: Routes = [

  // =========================================================
  // MOVIMIENTOS DE FONDOS
  // Consignaciones, retiros y traslados
  // =========================================================
  {
    path: 'movimientos-fondos',
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
    loadComponent: () =>
      import('./autorizacion-comprobantes/autorizacion-comprobantes')
        .then(m => m.AutorizacionComprobantesComponent)
  }

];

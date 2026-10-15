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
  }

];

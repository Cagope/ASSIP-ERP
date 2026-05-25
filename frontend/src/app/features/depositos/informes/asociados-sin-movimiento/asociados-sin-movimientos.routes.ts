import { Routes } from '@angular/router';

export const ASOCIADOS_SIN_MOVIMIENTOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./asociados-sin-movimientos.component')
        .then(m => m.AsociadosSinMovimientosComponent)
  }
];

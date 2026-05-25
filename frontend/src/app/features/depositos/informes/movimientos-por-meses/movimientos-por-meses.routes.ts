import { Routes } from '@angular/router';

export const MOVIMIENTOS_POR_MESES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./movimientos-por-meses.component')
        .then(m => m.MovimientosPorMesesComponent)
  }
];

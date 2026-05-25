import { Routes } from '@angular/router';

export const MOVIMIENTOS_DIARIOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./movimientos-diarios.component')
        .then(m => m.MovimientosDiariosComponent)
  }
];

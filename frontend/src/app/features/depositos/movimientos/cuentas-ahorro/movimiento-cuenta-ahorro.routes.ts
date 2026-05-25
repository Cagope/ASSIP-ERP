import { Routes } from '@angular/router';

export const MOVIMIENTO_CUENTA_AHORRO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./movimiento-cuenta-ahorro.component')
        .then(m => m.MovimientoCuentaAhorroComponent)
  }
];

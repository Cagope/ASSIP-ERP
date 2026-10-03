import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const MOVIMIENTO_CUENTA_AHORRO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./movimiento-cuenta-ahorro.component')
        .then(m => m.MovimientoCuentaAhorroComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_MOVIMIENTOS_VIEW'
    }
  }
];

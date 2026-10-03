import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const ASOCIADOS_SIN_MOVIMIENTOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./asociados-sin-movimientos.component')
        .then(m => m.AsociadosSinMovimientosComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

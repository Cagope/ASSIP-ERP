import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const MOVIMIENTOS_POR_MESES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./movimientos-por-meses.component')
        .then(m => m.MovimientosPorMesesComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

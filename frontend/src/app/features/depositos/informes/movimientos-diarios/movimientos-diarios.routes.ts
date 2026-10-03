import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const MOVIMIENTOS_DIARIOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./movimientos-diarios.component')
        .then(m => m.MovimientosDiariosComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const ESTADISTICOS_ASOCIADOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./estadisticos-asociados.component')
        .then(m => m.EstadisticosAsociadosComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

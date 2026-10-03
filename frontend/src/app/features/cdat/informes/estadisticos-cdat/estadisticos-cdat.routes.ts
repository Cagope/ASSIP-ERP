import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const ESTADISTICOS_CDAT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./estadisticos-cdat.component')
        .then(m => m.EstadisticosCdatComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_INFORMES_VIEW'
    }
  }
];

import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const FECHAS_CDAT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./fechas-cdat.component')
        .then(m => m.FechasCdatComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_INFORMES_VIEW'
    }
  }
];

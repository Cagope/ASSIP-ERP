import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const PROMEDIOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./promedios.component')
        .then(m => m.PromediosComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

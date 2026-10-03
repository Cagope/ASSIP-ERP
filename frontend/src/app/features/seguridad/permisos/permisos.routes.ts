import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

export const PERMISOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./permisos-list.component')
        .then(m => m.PermisosListComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'PERMISOS_VIEW'
    }
  }
];

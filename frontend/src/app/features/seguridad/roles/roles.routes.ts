import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

export const ROLES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./roles-list.component')
        .then(m => m.RolesListComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'ROLES_VIEW'
    }
  }
];

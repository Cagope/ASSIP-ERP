import { Routes } from '@angular/router';

import { UsuariosListComponent } from './usuarios-list.component';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const USUARIOS_ROUTES: Routes = [
  {
    path: '',
    component: UsuariosListComponent,
  },
  {
    path: 'nuevo',
    canActivate: [permisoGuard],
    data: {
      permiso: 'USUARIOS_EDIT'
    },
    loadComponent: () =>
      import('./usuarios-upsert.component').then(
        (m) => m.UsuariosUpsertComponent
      ),
  },
  {
    path: ':id',
    canActivate: [permisoGuard],
    data: {
      permiso: 'USUARIOS_EDIT'
    },
    loadComponent: () =>
      import('./usuarios-upsert.component').then(
        (m) => m.UsuariosUpsertComponent
      ),
  },
];

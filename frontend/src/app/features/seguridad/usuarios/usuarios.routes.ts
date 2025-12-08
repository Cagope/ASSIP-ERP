import { Routes } from '@angular/router';
import { UsuariosListComponent } from './usuarios-list.component';

export const USUARIOS_ROUTES: Routes = [
  {
    path: '',
    component: UsuariosListComponent,
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./usuarios-upsert.component').then(
        (m) => m.UsuariosUpsertComponent
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./usuarios-upsert.component').then(
        (m) => m.UsuariosUpsertComponent
      ),
  },
];

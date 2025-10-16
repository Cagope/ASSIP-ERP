import { Routes } from '@angular/router';

export const AGENCIAS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./agencias-list.component')
        .then(m => m.AgenciasListComponent),
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./agencias-upsert.component')
        .then(m => m.AgenciasUpsertComponent),
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./agencias-upsert.component')
        .then(m => m.AgenciasUpsertComponent),
  },
];

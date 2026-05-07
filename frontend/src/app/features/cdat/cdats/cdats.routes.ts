import { Routes } from '@angular/router';

export const CDATS_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./cdats-list.component')
        .then(m => m.CdatsListComponent),
  },

  {
    path: 'nuevo',
    loadComponent: () =>
      import('./cdats-upsert.component')
        .then(m => m.CdatsUpsertComponent),
  },

  {
    path: 'editar/:id',
    loadComponent: () =>
      import('./cdats-upsert.component')
        .then(m => m.CdatsUpsertComponent),
  }

];

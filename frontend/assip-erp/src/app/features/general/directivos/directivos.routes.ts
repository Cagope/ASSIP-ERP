import { Routes } from '@angular/router';

// Dentro del layout
export const DIRECTIVOS_ROUTES: Routes = [
  {
    path: '',
    data: { feature: 'directivos', action: 'read' },
    loadComponent: () =>
      import('./directivos-list.component')
        .then(m => m.DirectivosListComponent),
  },
  {
    path: 'new',
    data: { feature: 'directivos', action: 'create' },
    loadComponent: () =>
      import('./directivos-upsert.component')
        .then(m => m.DirectivosUpsertComponent),
  },
  {
    path: ':id/edit',
    data: { feature: 'directivos', action: 'update' },
    loadComponent: () =>
      import('./directivos-upsert.component')
        .then(m => m.DirectivosUpsertComponent),
  },
];

// Impresión (top-level, SIN layout)
export const DIRECTIVOS_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./directivos-print.component')
        .then(m => m.DirectivosPrintComponent),
  },
];

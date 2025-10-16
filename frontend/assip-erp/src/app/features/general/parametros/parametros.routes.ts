import { Routes } from '@angular/router';

// Dentro del layout
export const PARAMETROS_ROUTES: Routes = [
  {
    path: '',
    data: { feature: 'parametros', action: 'read' },
    loadComponent: () =>
      import('./parametros-list.component')
        .then(m => m.ParametrosListComponent),
  },
  {
    path: 'new',
    data: { feature: 'parametros', action: 'create' },
    loadComponent: () =>
      import('./parametros-upsert.component')
        .then(m => m.ParametrosUpsertComponent),
  },
  {
    path: ':id/edit',
    data: { feature: 'parametros', action: 'update' },
    loadComponent: () =>
      import('./parametros-upsert.component')
        .then(m => m.ParametrosUpsertComponent),
  },
];

// Impresión (top-level, sin layout)
export const PARAMETROS_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./parametros-print.component')
        .then(m => m.ParametrosPrintComponent),
  },
];

import { Routes } from '@angular/router';

// Dentro del layout
export const PRIVILEGIADOS_ROUTES: Routes = [
  {
    path: '',
    data: { feature: 'privilegiados', action: 'read' },
    loadComponent: () =>
      import('./privilegiados-list.component')
        .then(m => m.PrivilegiadosListComponent),
  },
  {
    path: 'new',
    data: { feature: 'privilegiados', action: 'create' },
    loadComponent: () =>
      import('./privilegiados-upsert.component')
        .then(m => m.PrivilegiadosUpsertComponent),
  },
  {
    path: ':id/edit',
    data: { feature: 'privilegiados', action: 'update' },
    loadComponent: () =>
      import('./privilegiados-upsert.component')
        .then(m => m.PrivilegiadosUpsertComponent),
  },
];

// Impresión (top-level, SIN layout)
export const PRIVILEGIADOS_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./privilegiados-print.component')
        .then(m => m.PrivilegiadosPrintComponent),
  },
];

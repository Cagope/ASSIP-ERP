import { Routes } from '@angular/router';

// Dentro del layout
export const ZONAS_ROUTES: Routes = [
  {
    path: '',
    data: { feature: 'zonas', action: 'read' },
    loadComponent: () =>
      import('./zonas-list.component')
        .then(m => m.ZonasListComponent),
  },
  {
    path: 'new',
    data: { feature: 'zonas', action: 'create' },
    loadComponent: () =>
      import('./zonas-upsert.component')
        .then(m => m.ZonasUpsertComponent),
  },
  {
    path: ':id/edit',
    data: { feature: 'zonas', action: 'update' },
    loadComponent: () =>
      import('./zonas-upsert.component')
        .then(m => m.ZonasUpsertComponent),
  },
];

// Impresión (top-level, sin layout)
export const ZONAS_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./zonas-print.component')
        .then(m => m.ZonasPrintComponent),
  },
];

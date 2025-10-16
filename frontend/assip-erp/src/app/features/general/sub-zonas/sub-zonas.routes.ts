import { Routes } from '@angular/router';

// Dentro del layout
export const SUBZONAS_ROUTES: Routes = [
  {
    path: '',
    data: { feature: 'sub-zonas', action: 'read' },
    loadComponent: () =>
      import('./sub-zonas-list.component')
        .then(m => m.SubZonasListComponent),
  },
  {
    path: 'new',
    data: { feature: 'sub-zonas', action: 'create' },
    loadComponent: () =>
      import('./sub-zonas-upsert.component')
        .then(m => m.SubZonasUpsertComponent),
  },
  {
    path: ':id/edit',
    data: { feature: 'sub-zonas', action: 'update' },
    loadComponent: () =>
      import('./sub-zonas-upsert.component')
        .then(m => m.SubZonasUpsertComponent),
  },
];

// Impresión (top-level, sin layout)
export const SUBZONAS_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./sub-zonas-print.component')
        .then(m => m.SubZonasPrintComponent),
  },
];

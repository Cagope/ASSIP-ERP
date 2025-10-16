import { Routes } from '@angular/router';

// Dentro del layout
export const DATOS_PERSONALES_ROUTES: Routes = [
  {
    path: '',
    data: { feature: 'datos-personales', action: 'read' },
    loadComponent: () =>
      import('./datos-personales-list.component')
        .then(m => m.DatosPersonalesListComponent),
  },
  {
    path: 'new',
    data: { feature: 'datos-personales', action: 'create' },
    loadComponent: () =>
      import('./datos-personales-upsert.component')
        .then(m => m.DatosPersonalesUpsertComponent),
  },
  {
    path: ':id/edit',
    data: { feature: 'datos-personales', action: 'update' },
    loadComponent: () =>
      import('./datos-personales-upsert.component')
        .then(m => m.DatosPersonalesUpsertComponent),
  },
];

// Impresión (top-level, sin layout)
export const DATOS_PERSONALES_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./datos-personales-print.component')
        .then(m => m.DatosPersonalesPrintComponent),
  },
];

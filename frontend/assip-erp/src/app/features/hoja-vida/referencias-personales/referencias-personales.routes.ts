import { Routes } from '@angular/router';

// Dentro del layout
export const REFERENCIAS_PERSONALES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./referencias-personales-list.component')
        .then(m => m.ReferenciasPersonalesListComponent),
  },
  {
    path: ':idDatosPersonales/new',
    loadComponent: () =>
      import('./referencias-personales-upsert.component')
        .then(m => m.ReferenciasPersonalesUpsertComponent),
  },
  {
    path: ':idDatosPersonales/edit/:id',
    loadComponent: () =>
      import('./referencias-personales-upsert.component')
        .then(m => m.ReferenciasPersonalesUpsertComponent),
  },
  {
    path: ':idDatosPersonales',
    loadComponent: () =>
      import('./referencias-personales-list.component')
        .then(m => m.ReferenciasPersonalesListComponent),
  },
];

// Impresión (top-level, sin layout)
export const REFERENCIAS_PERSONALES_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./referencias-personales-print.component')
        .then(m => m.ReferenciasPersonalesPrintComponent),
  },
];

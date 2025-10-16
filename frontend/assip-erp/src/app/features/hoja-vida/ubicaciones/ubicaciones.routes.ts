import { Routes } from '@angular/router';

// Dentro del layout (selector/lista + upsert)
export const UBICACIONES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./ubicaciones-list.component')
        .then(m => m.UbicacionesListComponent),
  },
  {
    path: ':idDatosPersonales/new',
    loadComponent: () =>
      import('./ubicaciones-upsert.component')
        .then(m => m.UbicacionesUpsertComponent),
  },
  {
    path: ':idDatosPersonales/edit/:idUbicacion',
    loadComponent: () =>
      import('./ubicaciones-upsert.component')
        .then(m => m.UbicacionesUpsertComponent),
  },
  {
    path: ':idDatosPersonales',
    loadComponent: () =>
      import('./ubicaciones-list.component')
        .then(m => m.UbicacionesListComponent),
  },
];

// Impresión (top-level, sin layout)
export const UBICACIONES_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./ubicaciones-print.component')
        .then(m => m.UbicacionesPrintComponent),
  },
];

import { Routes } from '@angular/router';

// Dentro del layout
export const PERMISOS_ESPECIALES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./permisos-especiales-list.component')
        .then(m => m.PermisosEspecialesListComponent),
  },
  {
    path: ':idDatosPersonales/new',
    loadComponent: () =>
      import('./permisos-especiales-upsert.component')
        .then(m => m.PermisosEspecialesUpsertComponent),
  },
  {
    path: ':idDatosPersonales/edit/:idPermiso',
    loadComponent: () =>
      import('./permisos-especiales-upsert.component')
        .then(m => m.PermisosEspecialesUpsertComponent),
  },
  {
    path: ':idDatosPersonales',
    loadComponent: () =>
      import('./permisos-especiales-list.component')
        .then(m => m.PermisosEspecialesListComponent),
  },
];

// Impresión (top-level, sin layout)
export const PERMISOS_ESPECIALES_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./permisos-especiales-print.component')
        .then(m => m.PermisosEspecialesPrintComponent),
  },
];

// src/app/features/hoja-vida/laborales/laborales.routes.ts
import { Routes } from '@angular/router';

export const LABORALES_ROUTES: Routes = [
  // Listado
  {
    path: '',
    loadComponent: () =>
      import('./laborales-list.component').then(m => m.LaboralesListComponent),
  },

  // Upsert (modo compat): /hoja-vida/laborales/:idDatosPersonales
  // Se usa tanto para editar como para crear (si viene ?create=1)
  {
    path: ':idDatosPersonales',
    loadComponent: () =>
      import('./laborales-upsert.component').then(m => m.LaboralesUpsertComponent),
  },

  // Upsert (explícito): /hoja-vida/laborales/:idDatosPersonales/edit
  // Deja más claro en la URL que estás en modo edición/creación.
  {
    path: ':idDatosPersonales/edit',
    loadComponent: () =>
      import('./laborales-upsert.component').then(m => m.LaboralesUpsertComponent),
  },
];

export const LABORALES_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./laborales-print.component').then(m => m.LaboralesPrintComponent),
  },
];

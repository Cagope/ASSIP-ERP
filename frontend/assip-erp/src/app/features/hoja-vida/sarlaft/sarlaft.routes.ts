import { Routes } from '@angular/router';

export const SARLAFT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./sarlaft-list.component').then(m => m.SarlaftListComponent),
  },
  {
    path: ':idDatosPersonales/new',
    loadComponent: () =>
      import('./sarlaft-upsert.component').then(m => m.SarlaftUpsertComponent),
  },
  {
    path: ':idDatosPersonales/edit/:idSarlaft',
    loadComponent: () =>
      import('./sarlaft-upsert.component').then(m => m.SarlaftUpsertComponent),
  },
  {
    path: ':idDatosPersonales',
    loadComponent: () =>
      import('./sarlaft-list.component').then(m => m.SarlaftListComponent),
  },
];

export const SARLAFT_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./sarlaft-print.component').then(m => m.SarlaftPrintComponent),
  },
];

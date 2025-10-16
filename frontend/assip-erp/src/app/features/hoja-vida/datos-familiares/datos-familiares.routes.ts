import { Routes } from '@angular/router';
import { authGuard } from '../../../core/guards/auth.guard';

// Dentro del layout
export const DATOS_FAMILIARES_ROUTES: Routes = [
  {
    path: '',
    canMatch: [authGuard],
    loadComponent: () =>
      import('./datos-familiares-list.component')
        .then(m => m.DatosFamiliaresListComponent),
  },
  {
    path: ':idDatosPersonales/new',
    canMatch: [authGuard],
    loadComponent: () =>
      import('./datos-familiares-upsert.component')
        .then(m => m.DatosFamiliaresUpsertComponent),
  },
  {
    path: ':idDatosPersonales/edit/:id',
    canMatch: [authGuard],
    loadComponent: () =>
      import('./datos-familiares-upsert.component')
        .then(m => m.DatosFamiliaresUpsertComponent),
  },
  {
    path: ':idDatosPersonales',
    canMatch: [authGuard],
    loadComponent: () =>
      import('./datos-familiares-list.component')
        .then(m => m.DatosFamiliaresListComponent),
  },
];

// Impresión (top-level, sin layout) — protegido con authGuard
export const DATOS_FAMILIARES_PRINT_ROUTES: Routes = [
  {
    path: '',
    canMatch: [authGuard],
    loadComponent: () =>
      import('./datos-familiares-print.component')
        .then(m => m.DatosFamiliaresPrintComponent),
  },
];

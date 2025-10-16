import { Routes } from '@angular/router';

// Dentro del layout
export const FINANCIEROS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./financieros-list.component')
        .then(m => m.FinancierosListComponent),
  },
  // NUEVA (modo crear)
  {
    path: ':idDatosPersonales/new',
    loadComponent: () =>
      import('./financieros-upsert.component')
        .then(m => m.FinancierosUpsertComponent),
  },
  // NUEVA RUTA (modo gestionar/editar SIN pedir id del financiero)
  {
    path: ':idDatosPersonales/edit',
    loadComponent: () =>
      import('./financieros-upsert.component')
        .then(m => m.FinancierosUpsertComponent),
  },
  // (opcional) editar por id específico, si en algún flujo lo necesitas
  {
    path: ':idDatosPersonales/edit/:idFinanciero',
    loadComponent: () =>
      import('./financieros-upsert.component')
        .then(m => m.FinancierosUpsertComponent),
  },
  // Detalle/selector por persona (se deja para tu vista read-only si la usas)
  {
    path: ':idDatosPersonales',
    loadComponent: () =>
      import('./financieros-list.component')
        .then(m => m.FinancierosListComponent),
  },
];

// Impresión (top-level, sin layout)
export const FINANCIEROS_PRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./financieros-print.component')
        .then(m => m.FinancierosPrintComponent),
  },
];

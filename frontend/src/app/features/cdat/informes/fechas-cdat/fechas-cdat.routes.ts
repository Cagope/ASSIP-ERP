import { Routes } from '@angular/router';

export const FECHAS_CDAT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./fechas-cdat.component')
        .then(m => m.FechasCdatComponent)
  }
];

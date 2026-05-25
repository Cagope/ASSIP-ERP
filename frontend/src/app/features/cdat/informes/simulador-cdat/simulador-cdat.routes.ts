import { Routes } from '@angular/router';

export const SIMULADOR_CDAT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./simulador-cdat.component')
        .then(m => m.SimuladorCdatComponent),
  }
];

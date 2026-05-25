import { Routes } from '@angular/router';

export const SALDOS_MENORES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./saldos-menores.component')
        .then(m => m.SaldosMenoresComponent)
  }
];

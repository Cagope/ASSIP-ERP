import { Routes } from '@angular/router';

export const SALDOS_RANGOS_EDAD_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./saldos-rangos-edad.component')
        .then(m => m.SaldosRangosEdadComponent)
  }
];

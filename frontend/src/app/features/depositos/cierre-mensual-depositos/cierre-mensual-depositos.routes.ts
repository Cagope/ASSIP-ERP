import { Routes } from '@angular/router';

export const CIERRE_MENSUAL_DEPOSITOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./cierre-mensual-depositos.component')
        .then(m => m.CierreMensualDepositosComponent)
  }
];

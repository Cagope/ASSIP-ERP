import { Routes } from '@angular/router';

export const CAPTURA_DEPOSITOS_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./captura-depositos.component')
        .then(m => m.CapturaDepositosComponent),
  }

];

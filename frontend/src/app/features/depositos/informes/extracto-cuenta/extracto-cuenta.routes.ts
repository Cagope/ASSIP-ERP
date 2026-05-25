import { Routes } from '@angular/router';

export const EXTRACTO_CUENTA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./extracto-cuenta.component')
        .then(m => m.ExtractoCuentaComponent)
  }
];

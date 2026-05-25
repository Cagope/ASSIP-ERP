import { Routes } from '@angular/router';

export const EXTRACTO_ASOCIADO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./extracto-asociado.component')
        .then(m => m.ExtractoAsociadoComponent)
  }
];

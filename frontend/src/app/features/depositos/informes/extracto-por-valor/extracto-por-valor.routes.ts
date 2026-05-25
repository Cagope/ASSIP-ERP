import { Routes } from '@angular/router';

export const EXTRACTO_POR_VALOR_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./extracto-por-valor.component')
        .then(m => m.ExtractoPorValorComponent)
  }
];

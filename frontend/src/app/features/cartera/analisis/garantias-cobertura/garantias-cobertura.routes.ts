import { Routes } from '@angular/router';

export const GARANTIAS_COBERTURA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./garantias-cobertura.component')
        .then(m => m.GarantiasCoberturaComponent)
  }
];

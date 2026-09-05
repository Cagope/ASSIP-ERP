import { Routes } from '@angular/router';

export const RECIPROCIDAD_APORTES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./reciprocidad-aportes.component')
        .then(m => m.ReciprocidadAportesComponent)
  }
];

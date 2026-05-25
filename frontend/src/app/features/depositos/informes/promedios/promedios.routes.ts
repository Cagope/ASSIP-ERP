import { Routes } from '@angular/router';

export const PROMEDIOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./promedios.component')
        .then(m => m.PromediosComponent)
  }
];

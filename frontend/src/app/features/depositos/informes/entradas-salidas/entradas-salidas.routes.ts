import { Routes } from '@angular/router';

export const ENTRADAS_SALIDAS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./entradas-salidas.component')
        .then(m => m.EntradasSalidasComponent)
  }
];

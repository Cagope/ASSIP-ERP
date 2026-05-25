import { Routes } from '@angular/router';

export const ESTADISTICOS_CDAT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./estadisticos-cdat.component')
        .then(m => m.EstadisticosCdatComponent)
  }
];

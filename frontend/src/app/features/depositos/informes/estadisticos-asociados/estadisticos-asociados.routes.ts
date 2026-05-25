import { Routes } from '@angular/router';

export const ESTADISTICOS_ASOCIADOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./estadisticos-asociados.component')
        .then(m => m.EstadisticosAsociadosComponent)
  }
];

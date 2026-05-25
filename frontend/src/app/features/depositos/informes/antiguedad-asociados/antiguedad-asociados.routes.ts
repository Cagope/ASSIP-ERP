import { Routes } from '@angular/router';

export const ANTIGUEDAD_ASOCIADOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./antiguedad-asociados.component')
        .then(m => m.AntiguedadAsociadosComponent)
  }
];

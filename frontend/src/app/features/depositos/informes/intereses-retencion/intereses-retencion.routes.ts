import { Routes } from '@angular/router';

export const INTERESES_RETENCION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./intereses-retencion.component')
        .then(m => m.InteresesRetencionComponent)
  }
];

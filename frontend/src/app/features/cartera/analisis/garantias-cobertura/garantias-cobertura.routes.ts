import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const GARANTIAS_COBERTURA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./garantias-cobertura.component')
        .then(m => m.GarantiasCoberturaComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ANALISIS_VIEW'
    }
  }
];

import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const RECIPROCIDAD_APORTES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./reciprocidad-aportes.component')
        .then(m => m.ReciprocidadAportesComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ANALISIS_VIEW'
    }
  }
];

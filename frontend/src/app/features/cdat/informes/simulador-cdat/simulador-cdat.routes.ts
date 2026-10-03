import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const SIMULADOR_CDAT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./simulador-cdat.component')
        .then(m => m.SimuladorCdatComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_INFORMES_VIEW'
    }
  }
];

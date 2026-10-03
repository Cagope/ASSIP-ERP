import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const CUMPLEANIOS_ASOCIADOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./cumpleanios-asociados.component')
        .then(m => m.CumpleaniosAsociadosComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

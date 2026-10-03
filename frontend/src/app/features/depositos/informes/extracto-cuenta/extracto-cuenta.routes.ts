import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const EXTRACTO_CUENTA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./extracto-cuenta.component')
        .then(m => m.ExtractoCuentaComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

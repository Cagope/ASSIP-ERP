import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const EXTRACTO_ASOCIADO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./extracto-asociado.component')
        .then(m => m.ExtractoAsociadoComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

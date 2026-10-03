import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const ANTIGUEDAD_ASOCIADOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./antiguedad-asociados.component')
        .then(m => m.AntiguedadAsociadosComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

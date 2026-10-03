import { Routes } from '@angular/router';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const CDAT_CANCELACION_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./cdat-cancelacion.component')
        .then(m => m.CdatCancelacionComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_CANCELACION_EXECUTE'
    }
  }

];

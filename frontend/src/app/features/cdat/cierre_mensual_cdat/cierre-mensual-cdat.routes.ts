import { Routes } from '@angular/router';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const CIERRE_MENSUAL_CDAT_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./cierre-mensual-cdat.component')
        .then(m => m.CierreMensualCdatComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_CIERRE_MENSUAL_EXECUTE'
    }
  }

];

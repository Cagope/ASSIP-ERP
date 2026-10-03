import { Routes } from '@angular/router';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const CAUSACION_MENSUAL_CDAT_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./causacion-mensual-cdat.component')
        .then(m => m.CausacionMensualCdatComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_CAUSACION_MENSUAL_EXECUTE'
    }
  }

];

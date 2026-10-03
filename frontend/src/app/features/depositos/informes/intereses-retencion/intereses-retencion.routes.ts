import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const INTERESES_RETENCION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./intereses-retencion.component')
        .then(m => m.InteresesRetencionComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

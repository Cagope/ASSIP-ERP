import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const GMF_SEMANAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./gmf-semanal.component')
        .then(m => m.GmfSemanalComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const CONSULTA_CDATS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./consulta-cdats-list.component')
        .then(m => m.ConsultaCdatsListComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_INFORMES_VIEW'
    }
  }
];

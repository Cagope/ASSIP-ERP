import { Routes } from '@angular/router';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const CIERRE_MENSUAL_DEPOSITOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./cierre-mensual-depositos.component')
        .then(m => m.CierreMensualDepositosComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_CIERRE_MENSUAL_EXECUTE'
    }
  }
];

import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const SALDOS_MENORES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./saldos-menores.component')
        .then(m => m.SaldosMenoresComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

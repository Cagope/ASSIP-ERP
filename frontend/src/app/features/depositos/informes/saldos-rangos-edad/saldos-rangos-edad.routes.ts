import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const SALDOS_RANGOS_EDAD_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./saldos-rangos-edad.component')
        .then(m => m.SaldosRangosEdadComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

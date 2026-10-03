import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const ENTRADAS_SALIDAS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./entradas-salidas.component')
        .then(m => m.EntradasSalidasComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

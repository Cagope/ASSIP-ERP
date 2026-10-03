import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const EXTRACTO_POR_VALOR_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./extracto-por-valor.component')
        .then(m => m.ExtractoPorValorComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

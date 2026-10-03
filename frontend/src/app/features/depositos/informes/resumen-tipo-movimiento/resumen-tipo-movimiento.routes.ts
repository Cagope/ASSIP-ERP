import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const RESUMEN_TIPO_MOVIMIENTO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./resumen-tipo-movimiento.component')
        .then(m => m.ResumenTipoMovimientoComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

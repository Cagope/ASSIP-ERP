import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

/** Protege el acceso a informes especiales de Cartera. */
export const INFORME_REVERSION_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permisoGuard],
    data: { permiso: 'CARTERA_INFORMES_ESPECIALES_VIEW' },
    loadComponent: () => import('./informe-reversion.component')
      .then(m => m.InformeReversionComponent)
  }
];

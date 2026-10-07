import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const CONSOLIDADO_CONCEPTOS_INFORME_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_INFORMES_VIEW' },
    loadComponent: () =>
      import('./consolidado-conceptos-informe.component')
        .then(m => m.ConsolidadoConceptosInformeComponent)
  }
];

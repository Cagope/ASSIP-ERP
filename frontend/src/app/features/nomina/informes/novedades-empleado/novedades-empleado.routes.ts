import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const NOVEDADES_EMPLEADO_INFORME_ROUTES: Routes = [
  {
    path: '',
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_INFORMES_VIEW' },
    loadComponent: () =>
      import('./novedades-empleado-informe.component')
        .then(m => m.NovedadesEmpleadoInformeComponent)
  }
];

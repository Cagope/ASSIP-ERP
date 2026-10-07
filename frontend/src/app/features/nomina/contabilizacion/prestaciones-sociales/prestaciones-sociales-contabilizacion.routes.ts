import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';

import { PrestacionesSocialesContabilizacionComponent } from './prestaciones-sociales-contabilizacion.component';

export const PRESTACIONES_SOCIALES_CONTABILIZACION_ROUTES: Routes = [
  {
    path: '',
    component: PrestacionesSocialesContabilizacionComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_CONTABILIZACION_EXECUTE' }
  }
];

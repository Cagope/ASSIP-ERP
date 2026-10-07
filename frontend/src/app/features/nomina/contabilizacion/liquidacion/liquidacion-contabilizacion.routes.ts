import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';
import { LiquidacionContabilizacionComponent } from './liquidacion-contabilizacion.component';

export const LIQUIDACION_CONTABILIZACION_ROUTES: Routes = [
  {
    path: '',
    component: LiquidacionContabilizacionComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_CONTABILIZACION_EXECUTE' }
  }
];

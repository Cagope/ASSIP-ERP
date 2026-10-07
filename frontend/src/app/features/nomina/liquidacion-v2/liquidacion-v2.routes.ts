import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { LiquidacionV2PreviewComponent } from './preview/liquidacion-v2-preview.component';

export const LIQUIDACION_V2_ROUTES: Routes = [
  {
    path: '',
    component: LiquidacionV2PreviewComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_LIQUIDACION_EXECUTE' }
  }
];

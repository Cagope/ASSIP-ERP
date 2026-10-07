import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { LiquidacionPreviewComponent } from './preview/liquidacion-preview.component';

export const LIQUIDACION_ROUTES: Routes = [

  // =========================================
  // 👁️ LIQUIDACIÓN DE NÓMINA (PREVIEW + ACCIONES)
  // =========================================
  {
    path: '',
    component: LiquidacionPreviewComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_LIQUIDACION_EXECUTE' }
  }

];

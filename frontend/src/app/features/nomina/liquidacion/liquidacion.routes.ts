import { Routes } from '@angular/router';
import { LiquidacionPreviewComponent } from './preview/liquidacion-preview.component';

export const LIQUIDACION_ROUTES: Routes = [

  // =========================================
  // 👁️ LIQUIDACIÓN DE NÓMINA (PREVIEW + ACCIONES)
  // =========================================
  {
    path: '',
    component: LiquidacionPreviewComponent
  }

];

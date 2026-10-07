import { Routes } from '@angular/router';

import {
  CentralRiesgoResultadoImportComponent
} from './central-riesgo-resultado-import.component';

import {
  permisoGuard
} from '../../../../../core/auth/permiso.guard';

export const CENTRAL_RIESGO_RESULTADO_ROUTES: Routes = [
  {
    path: '',
    component: CentralRiesgoResultadoImportComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_EVALUACION_VIEW'
    }
  }
];

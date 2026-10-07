import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { EventosLiquidacionComponent } from './eventos-liquidacion.component';
import { EventosLiquidacionUpsertComponent } from './eventos-liquidacion-upsert.component';

export const EVENTOS_LIQUIDACION_ROUTES: Routes = [
  {
    path: '',
    component: EventosLiquidacionComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },
  {
    path: 'nuevo',
    component: EventosLiquidacionUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  },
  {
    path: ':id/editar',
    component: EventosLiquidacionUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  }
];

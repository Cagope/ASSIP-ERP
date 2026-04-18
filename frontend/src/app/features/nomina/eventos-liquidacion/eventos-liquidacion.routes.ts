import { Routes } from '@angular/router';
import { EventosLiquidacionComponent } from './eventos-liquidacion.component';
import { EventosLiquidacionUpsertComponent } from './eventos-liquidacion-upsert.component';

export const EVENTOS_LIQUIDACION_ROUTES: Routes = [
  {
    path: '',
    component: EventosLiquidacionComponent
  },
  {
    path: 'nuevo',
    component: EventosLiquidacionUpsertComponent
  },
  {
    path: ':id/editar',
    component: EventosLiquidacionUpsertComponent
  }
];

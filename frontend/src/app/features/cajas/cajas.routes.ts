import { Routes } from '@angular/router';

import { permisoGuard } from '../../core/auth/permiso.guard';

import { CAJAS_PROVISION_ROUTES } from './provisiones/cajas-provision.routes';
import { CAPTURA_DEPOSITOS_ROUTES } from './captura_deposito/captura-depositos.routes';
import { CONVENIOS_RECAUDO_ROUTES } from './convenios-recaudo/convenios-recaudo.routes';
import { RECAUDOS_CONVENIOS_ROUTES } from './recaudos-convenios/recaudos-convenios.routes';
import { CIERRE_RECAUDOS_CONVENIOS_ROUTES } from './cierre-recaudos-convenios/cierre-recaudos-convenios.routes';
import { MOVIMIENTOS_INTERAGENCIA_ROUTES } from './movimientos-interagencia/movimientos-interagencia.routes';
import { OPERACIONES_PENDIENTES_ROUTES } from './operaciones-pendientes/operaciones-pendientes.routes';


export const CAJAS_ROUTES: Routes = [

  {
    path: 'provisiones',
    canActivate: [permisoGuard],
    data: { permiso: 'CAJAS_VINCULAR_CAJA' },
    children: CAJAS_PROVISION_ROUTES
  },

  {
    path: 'captura_depositos',
    children: CAPTURA_DEPOSITOS_ROUTES
  },

  {
    path: 'convenios-recaudo',
    children: CONVENIOS_RECAUDO_ROUTES
  },

  {
    path: 'recaudos-convenios',
    children: RECAUDOS_CONVENIOS_ROUTES
  },

  {
    path: 'cierre-recaudos-convenios',
    children: CIERRE_RECAUDOS_CONVENIOS_ROUTES
  },

  {
    path: 'movimientos-interagencia',
    children: MOVIMIENTOS_INTERAGENCIA_ROUTES
  },

  {
    path: 'operaciones-pendientes',
    canActivate: [permisoGuard],
    data: { permiso: 'CAJAS_OPERACIONES_PENDIENTES_VIEW' },
    children: OPERACIONES_PENDIENTES_ROUTES
  }

];

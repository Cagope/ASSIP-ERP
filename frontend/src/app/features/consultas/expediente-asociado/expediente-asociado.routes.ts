import { Routes } from '@angular/router';

import {
  permisoGuard
} from '../../../core/auth/permiso.guard';

export const EXPEDIENTE_ASOCIADO_ROUTES: Routes = [

  {
    path: '',
    canActivate: [permisoGuard],
    data: {
      permiso: 'EXPEDIENTE_ASOCIADO_VIEW'
    },
    loadComponent: () =>
      import('./list/expediente-asociado-list.component')
        .then(m => m.ExpedienteAsociadoListComponent)
  },

  {
    path: ':idDatosPersonal',
    canActivate: [permisoGuard],
    data: {
      permiso: 'EXPEDIENTE_ASOCIADO_VIEW'
    },
    loadComponent: () =>
      import('./expediente-asociado.component')
        .then(m => m.ExpedienteAsociadoComponent)
  }

];

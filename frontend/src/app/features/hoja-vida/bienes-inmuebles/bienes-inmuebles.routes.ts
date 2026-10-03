import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../core/auth/permiso.guard';

import {
  BienesInmueblesListComponent
} from './bienes-inmuebles-list.component';

import {
  BienesInmueblesUpsertComponent
} from './bienes-inmuebles-upsert.component';

export const BIENES_INMUEBLES_ROUTES: Routes = [

  {
    path: '',
    component: BienesInmueblesListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_BIENES_VIEW'
    }
  },

  {
    path: 'persona/:idDatosPersonal/gestionar',
    component: BienesInmueblesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_BIENES_VIEW'
    }
  }

];

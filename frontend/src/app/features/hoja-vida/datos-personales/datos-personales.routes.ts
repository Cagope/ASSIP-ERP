import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import {
  DatosPersonalesListComponent
} from './datos-personales-list.component';

import {
  DatosPersonalesUpsertComponent
} from './datos-personales-upsert.component';

export const DATOS_PERSONALES_ROUTES: Routes = [

  {
    path: '',
    component: DatosPersonalesListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: DatosPersonalesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: DatosPersonalesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  }

];

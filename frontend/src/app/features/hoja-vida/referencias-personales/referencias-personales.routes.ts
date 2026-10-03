import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { ReferenciasPersonalesListComponent } from './referencias-personales-list.component';
import { ReferenciasPersonalesUpsertComponent } from './referencias-personales-upsert.component';

export const REFERENCIAS_PERSONALES_ROUTES: Routes = [

  {
    path: '',
    component: ReferenciasPersonalesListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: ReferenciasPersonalesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: ReferenciasPersonalesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  }

];

import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { UbicacionesListComponent } from './ubicaciones-list.component';
import { UbicacionesUpsertComponent } from './ubicaciones-upsert.component';

export const UBICACIONES_ROUTES: Routes = [

  {
    path: '',
    component: UbicacionesListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  },

  {
    path: 'nuevo',
    component: UbicacionesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_EDIT'
    }
  },

  {
    path: ':id/editar',
    component: UbicacionesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    }
  }

];

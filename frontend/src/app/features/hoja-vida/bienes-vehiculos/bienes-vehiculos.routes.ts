import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { BienesVehiculosListComponent } from './bienes-vehiculos-list.component';
import { BienesVehiculosUpsertComponent } from './bienes-vehiculos-upsert.component';

export const BIENES_VEHICULOS_ROUTES: Routes = [
  {
    path: '',
    component: BienesVehiculosListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_BIENES_VIEW'
    }
  },
  {
    path: 'persona/:idDatosPersonal/gestionar',
    component: BienesVehiculosUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_BIENES_VIEW'
    }
  }
];

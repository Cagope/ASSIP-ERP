import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { BienesInversionesListComponent } from './bienes-inversiones-list.component';
import { BienesInversionesUpsertComponent } from './bienes-inversiones-upsert.component';

export const BIENES_INVERSIONES_ROUTES: Routes = [
  {
    path: '',
    component: BienesInversionesListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_BIENES_VIEW'
    }
  },
  {
    path: 'persona/:idDatosPersonal/gestionar',
    component: BienesInversionesUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_BIENES_VIEW'
    }
  }
];

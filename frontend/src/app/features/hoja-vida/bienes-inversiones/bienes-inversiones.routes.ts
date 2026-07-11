import { Routes } from '@angular/router';

import { BienesInversionesListComponent } from './bienes-inversiones-list.component';
import { BienesInversionesUpsertComponent } from './bienes-inversiones-upsert.component';

export const BIENES_INVERSIONES_ROUTES: Routes = [
  {
    path: '',
    component: BienesInversionesListComponent
  },
  {
    path: 'persona/:idDatosPersonal/gestionar',
    component: BienesInversionesUpsertComponent
  }
];

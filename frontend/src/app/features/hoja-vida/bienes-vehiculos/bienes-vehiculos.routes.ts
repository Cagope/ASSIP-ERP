import { Routes } from '@angular/router';

import { BienesVehiculosListComponent } from './bienes-vehiculos-list.component';
import { BienesVehiculosUpsertComponent } from './bienes-vehiculos-upsert.component';

export const BIENES_VEHICULOS_ROUTES: Routes = [
  { path: '', component: BienesVehiculosListComponent },
  {
    path: 'persona/:idDatosPersonal/gestionar',
    component: BienesVehiculosUpsertComponent
  },
];

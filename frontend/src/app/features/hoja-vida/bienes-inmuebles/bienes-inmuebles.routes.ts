import { Routes } from '@angular/router';

import { BienesInmueblesListComponent } from './bienes-inmuebles-list.component';
import { BienesInmueblesUpsertComponent } from './bienes-inmuebles-upsert.component';

export const BIENES_INMUEBLES_ROUTES: Routes = [
  { path: '', component: BienesInmueblesListComponent },
  { path: 'persona/:idDatosPersonal/gestionar', component: BienesInmueblesUpsertComponent },
];

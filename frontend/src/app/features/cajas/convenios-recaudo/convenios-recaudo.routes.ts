import { Routes } from '@angular/router';

import { ConveniosRecaudoListComponent } from './convenios-recaudo-list.component';
import { ConveniosRecaudoUpsertComponent } from './convenios-recaudo-upsert.component';

export const CONVENIOS_RECAUDO_ROUTES: Routes = [
  {
    path: '',
    component: ConveniosRecaudoListComponent
  },
  {
    path: 'nuevo',
    component: ConveniosRecaudoUpsertComponent
  },
  {
    path: ':id/editar',
    component: ConveniosRecaudoUpsertComponent
  }
];

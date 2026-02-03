import { Routes } from '@angular/router';
import { EpsListComponent } from './eps-list.component';
import { EpsUpsertComponent } from './eps-upsert.component';

export const EPS_ROUTES: Routes = [
  { path: '', component: EpsListComponent },
  { path: 'nuevo', component: EpsUpsertComponent },
  { path: ':id/editar', component: EpsUpsertComponent }
];

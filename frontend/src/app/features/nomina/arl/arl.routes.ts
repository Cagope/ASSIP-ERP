import { Routes } from '@angular/router';
import { ArlListComponent } from './arl-list.component';
import { ArlUpsertComponent } from './arl-upsert.component';

export const ARL_ROUTES: Routes = [
  { path: '', component: ArlListComponent },
  { path: 'nuevo', component: ArlUpsertComponent },
  { path: ':id/editar', component: ArlUpsertComponent }
];

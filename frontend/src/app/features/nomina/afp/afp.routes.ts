import { Routes } from '@angular/router';
import { AfpListComponent } from './afp-list.component';
import { AfpUpsertComponent } from './afp-upsert.component';

export const AFP_ROUTES: Routes = [
  { path: '', component: AfpListComponent },
  { path: 'nuevo', component: AfpUpsertComponent },
  { path: ':id/editar', component: AfpUpsertComponent }
];

import { Routes } from '@angular/router';
import { FormasAhorroListComponent } from './formas-ahorro-list.component';
import { FormasAhorroUpsertComponent } from './formas-ahorro-upsert.component';

export const FORMAS_AHORRO_ROUTES: Routes = [
  { path: '', component: FormasAhorroListComponent },
  { path: 'nuevo', component: FormasAhorroUpsertComponent },
  { path: ':id/editar', component: FormasAhorroUpsertComponent }
];

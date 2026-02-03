import { Routes } from '@angular/router';
import { BloquesListComponent } from './bloques-list.component';
import { BloquesUpsertComponent } from './bloques-upsert.component';

export const BLOQUES_ROUTES: Routes = [
  { path: '', component: BloquesListComponent },
  { path: 'nuevo', component: BloquesUpsertComponent },
  { path: ':id/editar', component: BloquesUpsertComponent }
];

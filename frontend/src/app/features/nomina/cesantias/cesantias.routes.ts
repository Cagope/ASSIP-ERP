import { Routes } from '@angular/router';
import { CesantiasListComponent } from './cesantias-list.component';
import { CesantiasUpsertComponent } from './cesantias-upsert.component';

export const CESANTIAS_ROUTES: Routes = [
  { path: '', component: CesantiasListComponent },
  { path: 'nuevo', component: CesantiasUpsertComponent },
  { path: ':id/editar', component: CesantiasUpsertComponent }
];

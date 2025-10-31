import { Routes } from '@angular/router';
import { ReferenciasPersonalesListComponent } from './referencias-personales-list.component';
import { ReferenciasPersonalesUpsertComponent } from './referencias-personales-upsert.component';

export const REFERENCIAS_PERSONALES_ROUTES: Routes = [
  { path: '', component: ReferenciasPersonalesListComponent },
  { path: 'nuevo', component: ReferenciasPersonalesUpsertComponent },
  { path: ':id/editar', component: ReferenciasPersonalesUpsertComponent },
];

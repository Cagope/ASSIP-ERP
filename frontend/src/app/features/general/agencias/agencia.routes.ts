import { Routes } from '@angular/router';
import { AgenciaListComponent } from './agencia-list.component';
import { AgenciaUpsertComponent } from './agencia-upsert.component';

export const AGENCIA_ROUTES: Routes = [
  { path: '', component: AgenciaListComponent },
  { path: 'nueva', component: AgenciaUpsertComponent },
  { path: ':id', component: AgenciaUpsertComponent },
];

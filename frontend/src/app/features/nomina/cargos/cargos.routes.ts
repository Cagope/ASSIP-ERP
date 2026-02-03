import { Routes } from '@angular/router';
import { CargosListComponent } from './cargos-list.component';
import { CargosUpsertComponent } from './cargos-upsert.component';

export const CARGOS_ROUTES: Routes = [
  { path: '', component: CargosListComponent },
  { path: 'nuevo', component: CargosUpsertComponent },
  { path: ':id/editar', component: CargosUpsertComponent }
];

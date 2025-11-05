import { Routes } from '@angular/router';
import { ParametroListComponent } from './parametro-list.component';
import { ParametroUpsertComponent } from './parametro-upsert.component';

export const PARAMETROS_ROUTES: Routes = [
  { path: '', component: ParametroListComponent },
  { path: 'nuevo', component: ParametroUpsertComponent },
  { path: ':id/editar', component: ParametroUpsertComponent },
];

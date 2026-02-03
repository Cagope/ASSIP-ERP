import { Routes } from '@angular/router';
import { VariablesVigenciaListComponent } from './variables-vigencia-list.component';
import { VariablesVigenciaUpsertComponent } from './variables-vigencia-upsert.component';

export const VARIABLES_VIGENCIA_ROUTES: Routes = [
  { path: '', component: VariablesVigenciaListComponent },
  { path: 'nuevo', component: VariablesVigenciaUpsertComponent },
  { path: ':id/editar', component: VariablesVigenciaUpsertComponent },
];

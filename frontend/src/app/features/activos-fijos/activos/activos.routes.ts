import { Routes } from '@angular/router';
import { ActivosFijosListComponent } from './activos-fijos-list.component';
import { ActivosFijosUpsertComponent } from './activos-fijos-upsert.component';

export const ACTIVOS_ROUTES: Routes = [
  { path: '', component: ActivosFijosListComponent },
  { path: 'nuevo', component: ActivosFijosUpsertComponent },
  { path: ':id/editar', component: ActivosFijosUpsertComponent }
];

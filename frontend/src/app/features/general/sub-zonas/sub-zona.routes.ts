import { Routes } from '@angular/router';
import { SubZonaListComponent } from './sub-zona-list.component';
import { SubZonaUpsertComponent } from './sub-zona-upsert.component';

export const SUB_ZONAS_ROUTES: Routes = [
  { path: '', component: SubZonaListComponent },
  { path: 'nuevo', component: SubZonaUpsertComponent },
  { path: ':id/editar', component: SubZonaUpsertComponent },
];

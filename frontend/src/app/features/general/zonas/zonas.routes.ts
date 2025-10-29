import { Routes } from '@angular/router';
import { ZonaListComponent } from './zona-list.component';
import { ZonaUpsertComponent } from './zona-upsert.component';

export const ZONAS_ROUTES: Routes = [
  { path: '', component: ZonaListComponent },
  { path: 'nuevo', component: ZonaUpsertComponent },
  { path: ':id/editar', component: ZonaUpsertComponent }, // ✅ corregido
];

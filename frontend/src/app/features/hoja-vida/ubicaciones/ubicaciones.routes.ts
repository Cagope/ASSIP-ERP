import { Routes } from '@angular/router';
import { UbicacionesListComponent } from './ubicaciones-list.component';
import { UbicacionesUpsertComponent } from './ubicaciones-upsert.component';

export const UBICACIONES_ROUTES: Routes = [
  { path: '', component: UbicacionesListComponent },
  { path: 'nuevo', component: UbicacionesUpsertComponent },
  { path: ':id/editar', component: UbicacionesUpsertComponent },
];

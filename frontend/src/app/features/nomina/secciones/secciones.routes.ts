import { Routes } from '@angular/router';
import { SeccionesListComponent } from './secciones-list.component';
import { SeccionesUpsertComponent } from './secciones-upsert.component';

export const SECCIONES_ROUTES: Routes = [
  { path: '', component: SeccionesListComponent },
  { path: 'nuevo', component: SeccionesUpsertComponent },
  { path: ':id/editar', component: SeccionesUpsertComponent },
];

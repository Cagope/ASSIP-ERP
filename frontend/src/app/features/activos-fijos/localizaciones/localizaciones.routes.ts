import { Routes } from '@angular/router';
import { LocalizacionesListComponent } from './localizaciones-list.component';
import { LocalizacionesUpsertComponent } from './localizaciones-upsert.component';

export const LOCALIZACIONES_ROUTES: Routes = [
  { path: '', component: LocalizacionesListComponent },
  { path: 'nuevo', component: LocalizacionesUpsertComponent },
  { path: ':id/editar', component: LocalizacionesUpsertComponent }
];

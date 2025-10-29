import { Routes } from '@angular/router';
import { DatosPersonalesListComponent } from './datos-personales-list.component';
import { DatosPersonalesUpsertComponent } from './datos-personales-upsert.component';

export const DATOS_PERSONALES_ROUTES: Routes = [
  { path: '', component: DatosPersonalesListComponent },
  { path: 'nuevo', component: DatosPersonalesUpsertComponent },
  { path: ':id/editar', component: DatosPersonalesUpsertComponent },
];

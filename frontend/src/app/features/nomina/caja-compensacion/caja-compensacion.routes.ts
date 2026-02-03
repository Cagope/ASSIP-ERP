import { Routes } from '@angular/router';
import { CajaCompensacionListComponent } from './caja-compensacion-list.component';
import { CajaCompensacionUpsertComponent } from './caja-compensacion-upsert.component';

export const CAJA_COMPENSACION_ROUTES: Routes = [
  { path: '', component: CajaCompensacionListComponent },
  { path: 'nuevo', component: CajaCompensacionUpsertComponent },
  { path: ':id/editar', component: CajaCompensacionUpsertComponent }
];

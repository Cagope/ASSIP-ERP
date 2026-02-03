import { Routes } from '@angular/router';
import { EmpleadosListComponent } from './empleados-list.component';
import { EmpleadosUpsertComponent } from './empleados-upsert.component';

export const EMPLEADOS_ROUTES: Routes = [
  { path: '', component: EmpleadosListComponent },
  { path: 'nuevo', component: EmpleadosUpsertComponent },
  { path: ':id/editar', component: EmpleadosUpsertComponent },
];

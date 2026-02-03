import { Routes } from '@angular/router';
import { EmpleadoContratosListComponent } from './empleado-contratos-list.component';
import { EmpleadoContratosUpsertComponent } from './empleado-contratos-upsert.component';

export const EMPLEADO_CONTRATOS_ROUTES: Routes = [
  { path: '', component: EmpleadoContratosListComponent },
  { path: 'nuevo', component: EmpleadoContratosUpsertComponent },
  { path: ':id/editar', component: EmpleadoContratosUpsertComponent },
];

import { Routes } from '@angular/router';
import { PlanCuentasListComponent } from './plan-cuentas-list.component';
import { PlanCuentasUpsertComponent } from './plan-cuentas-upsert.component';

export const PLAN_CUENTAS_ROUTES: Routes = [
  { path: '', component: PlanCuentasListComponent },
  { path: 'nuevo', component: PlanCuentasUpsertComponent },
  { path: ':id/editar', component: PlanCuentasUpsertComponent },
];

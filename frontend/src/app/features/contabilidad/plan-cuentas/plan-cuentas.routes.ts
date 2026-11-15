import { Routes } from '@angular/router';
import { permisoGuard } from '../../../core/auth/permiso.guard';
import { PlanCuentasListComponent } from './plan-cuentas-list.component';
import { PlanCuentasUpsertComponent } from './plan-cuentas-upsert.component';

export const PLAN_CUENTAS_ROUTES: Routes = [
  { path: '', component: PlanCuentasListComponent },
  { path: 'nuevo', component: PlanCuentasUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'CONTABILIDAD_PARAMETRIZACION_ADMIN' } },
  { path: ':id/editar', component: PlanCuentasUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'CONTABILIDAD_PARAMETRIZACION_ADMIN' } },
];

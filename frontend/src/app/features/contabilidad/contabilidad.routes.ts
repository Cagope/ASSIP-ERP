import { Routes } from '@angular/router';
import { PLAN_CUENTAS_ROUTES } from './plan-cuentas/plan-cuentas.routes';

export const CONTABILIDAD_ROUTES: Routes = [
  {
    path: 'plan-cuentas',
    children: PLAN_CUENTAS_ROUTES,
  },
];

import { Routes } from '@angular/router';
import { PLAN_CUENTAS_ROUTES } from './plan-cuentas/plan-cuentas.routes';
import { TIPOS_COMPROBANTES_ROUTES } from './tipos-comprobantes/tipos-comprobantes.routes';

export const CONTABILIDAD_ROUTES: Routes = [
  {
    path: 'plan-cuentas',
    children: PLAN_CUENTAS_ROUTES,
  },
  {
    path: 'tipos-comprobantes',
    children: TIPOS_COMPROBANTES_ROUTES,
  },
];

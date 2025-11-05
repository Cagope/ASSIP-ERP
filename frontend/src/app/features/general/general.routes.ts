import { Routes } from '@angular/router';
import { AGENCIA_ROUTES } from './agencias/agencia.routes';
import { ZONAS_ROUTES } from './zonas/zonas.routes';
import { SUB_ZONAS_ROUTES } from './sub-zonas/sub-zona.routes';
import { PARAMETROS_ROUTES } from './parametros/parametro.routes';

export const GENERAL_ROUTES: Routes = [
  {
    path: 'agencias',
    children: AGENCIA_ROUTES,
  },
  {
    path: 'zonas',
    children: ZONAS_ROUTES,
  },
  {
    path: 'sub-zonas',
    children: SUB_ZONAS_ROUTES,
  },
  {
    path: 'parametros',
    children: PARAMETROS_ROUTES,
  },
];

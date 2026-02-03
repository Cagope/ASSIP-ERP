import { Routes } from '@angular/router';

import { CARGOS_ROUTES } from './cargos/cargos.routes';
import { SECCIONES_ROUTES } from './secciones/secciones.routes';
import { EPS_ROUTES } from './eps/eps-routes';
import { AFP_ROUTES } from './afp/afp.routes';
import { ARL_ROUTES } from './arl/arl.routes';
import { CAJA_COMPENSACION_ROUTES } from './caja-compensacion/caja-compensacion.routes';
import { VARIABLES_VIGENCIA_ROUTES } from './variables-vigencia/variables-vigencia.routes';
import { CONCEPTOS_NOMINA_ROUTES } from './conceptos-nomina/conceptos-nomina.routes';
import { EMPLEADOS_ROUTES } from './empleados/empleados.routes';
import { EMPLEADO_CONTRATOS_ROUTES } from './empleado-contratos/empleado-contratos.routes';
import { CESANTIAS_ROUTES } from './cesantias/cesantias.routes';

export const NOMINA_ROUTES: Routes = [

  // ✅ CRUD: Cargos
  {
    path: 'cargos',
    children: CARGOS_ROUTES
  },

  // ✅ CRUD: Secciones Nómina
  {
    path: 'secciones',
    children: SECCIONES_ROUTES
  },

  // ✅ CRUD: EPS
  {
    path: 'eps',
    children: EPS_ROUTES
  },

  // ✅ CRUD: AFP
  {
    path: 'afp',
    children: AFP_ROUTES
  },

  // ✅ CRUD: ARL
  {
    path: 'arl',
    children: ARL_ROUTES
  },

  // ✅ CRUD: Caja de Compensación
  {
    path: 'caja-compensacion',
    children: CAJA_COMPENSACION_ROUTES
  },

  // ✅ CRUD: Variables de Vigencia
  {
    path: 'variables-vigencia',
    children: VARIABLES_VIGENCIA_ROUTES
  },

  // ✅ CRUD: Conceptos de Nómina
  {
    path: 'conceptos-nomina',
    children: CONCEPTOS_NOMINA_ROUTES
  },

  // ✅ CRUD: Empleados
  {
    path: 'empleados',
    children: EMPLEADOS_ROUTES
  },

  // ✅ CRUD: Cesantías
  {
    path: 'cesantias',
    children: CESANTIAS_ROUTES
  },

  // ✅ CRUD: Contratos de Empleados
  {
    path: 'empleado-contratos',
    children: EMPLEADO_CONTRATOS_ROUTES
  }

];

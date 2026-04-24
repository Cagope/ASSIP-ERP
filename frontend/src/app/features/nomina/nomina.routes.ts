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
import { DESPRENDIBLE_ROUTES } from './desprendible/desprendible.routes';

// ✅ NUEVO: Novedades Nómina
import { NOVEDADES_NOMINA_ROUTES } from './novedades-nomina/novedades-nomina.routes';

// ✅ NUEVO: Períodos Nómina
import { PERIODOS_NOMINA_ROUTES } from './periodos-nomina/periodos-nomina.routes';
import { LIQUIDACION_ROUTES } from './liquidacion/liquidacion.routes';
import { CONCEPTO_CUENTAS_CONTABLES_ROUTES } from './concepto-cuentas-contables/concepto-cuentas-contables.routes';
import { TIPOS_CONTRATOS_ROUTES } from './tipos-contratos/tipos-contratos.routes';
import { LIQUIDACION_V2_ROUTES } from './liquidacion-v2/liquidacion-v2.routes';

// ✅ NUEVO: Eventos de liquidación
import { EVENTOS_LIQUIDACION_ROUTES } from './eventos-liquidacion/eventos-liquidacion.routes';
import { NOVEDADES_EMPLEADO_INFORME_ROUTES } from './informes/novedades-empleado/novedades-empleado.routes';

import { NOVEDADES_CONCEPTO_INFORME_ROUTES } from './informes/novedades-concepto/novedades-concepto.routes';
import { CONSOLIDADO_CONCEPTOS_INFORME_ROUTES } from './informes/consolidado-conceptos/consolidado-conceptos.routes';

export const NOMINA_ROUTES: Routes = [

  // =========================
  // 🏗 ESTRUCTURA HUMANA
  // =========================

  {
    path: 'empleados',
    children: EMPLEADOS_ROUTES
  },

  {
    path: 'empleado-contratos',
    children: EMPLEADO_CONTRATOS_ROUTES
  },

  // =========================
  // 📅 CALENDARIO DE NÓMINA
  // =========================

  {
    path: 'periodos',
    children: PERIODOS_NOMINA_ROUTES
  },

  // =========================
  // 📄 DESPRENDIBLES DE NÓMINA
  // =========================
  {
    path: 'desprendible',
    children: DESPRENDIBLE_ROUTES
  },

  // =========================
  // ⚙ OPERACIÓN
  // =========================

  {
    path: 'novedades',
    children: NOVEDADES_NOMINA_ROUTES
  },

  {
    path: 'eventos-liquidacion',
    children: EVENTOS_LIQUIDACION_ROUTES
  },

  {
    path: 'liquidacion',
    children: LIQUIDACION_ROUTES
  },

  {
    path: 'cesantias',
    children: CESANTIAS_ROUTES
  },

  // =========================
  // 📚 CONFIGURACIÓN
  // =========================

  {
    path: 'conceptos-nomina',
    children: CONCEPTOS_NOMINA_ROUTES
  },

  {
    path: 'concepto-cuentas-contables',
    children: CONCEPTO_CUENTAS_CONTABLES_ROUTES
  },

  {
    path: 'variables-vigencia',
    children: VARIABLES_VIGENCIA_ROUTES
  },

  {
    path: 'cargos',
    children: CARGOS_ROUTES
  },

  {
    path: 'secciones',
    children: SECCIONES_ROUTES
  },

  {
    path: 'eps',
    children: EPS_ROUTES
  },

  {
    path: 'afp',
    children: AFP_ROUTES
  },

  {
    path: 'arl',
    children: ARL_ROUTES
  },

  {
    path: 'caja-compensacion',
    children: CAJA_COMPENSACION_ROUTES
  },

  {
    path: 'contabilizacion/liquidacion',
    loadChildren: () =>
      import('./contabilizacion/liquidacion/liquidacion-contabilizacion.routes')
        .then(m => m.LIQUIDACION_CONTABILIZACION_ROUTES)
  },

  {
    path: 'contabilizacion/aportes-parafiscales',
    loadChildren: () =>
      import('./contabilizacion/aportes-parafiscales/aportes-parafiscales-contabilizacion.routes')
        .then(m => m.APORTES_PARAFISCALES_CONTABILIZACION_ROUTES)
  },

  {
    path: 'contabilizacion/prestaciones-sociales',
    loadChildren: () =>
      import('./contabilizacion/prestaciones-sociales/prestaciones-sociales-contabilizacion.routes')
        .then(m => m.PRESTACIONES_SOCIALES_CONTABILIZACION_ROUTES)
  },

  {
    path: 'tipos-contratos',
    children: TIPOS_CONTRATOS_ROUTES
  },

  {
    path: 'liquidacion-v2',
    children: LIQUIDACION_V2_ROUTES
  },

  // =========================
  // 📊 INFORMES
  // =========================
  {
    path: 'informes/novedades-empleado',
    children: NOVEDADES_EMPLEADO_INFORME_ROUTES
  },
  {
    path: 'informes/novedades-concepto',
    children: NOVEDADES_CONCEPTO_INFORME_ROUTES
  },

  // =========================
  // 📊 INFORMES
  // =========================
  {
    path: 'informes/novedades-empleado',
    children: NOVEDADES_EMPLEADO_INFORME_ROUTES
  },
  {
    path: 'informes/novedades-concepto',
    children: NOVEDADES_CONCEPTO_INFORME_ROUTES
  },
  {
    path: 'informes/consolidado-conceptos',
    children: CONSOLIDADO_CONCEPTOS_INFORME_ROUTES
  }

];

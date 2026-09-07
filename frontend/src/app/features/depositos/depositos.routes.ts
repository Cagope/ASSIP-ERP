import { Routes } from '@angular/router';

// Consulta Cuentas de Ahorro (ANTES cuentas-ahorro)
import { CONSULTA_CUENTAS_AHORRO_ROUTES } from './informes/consulta-cuentas-ahorro/consulta-cuentas-ahorro.routes';

//Submódulo: Formas de Ahorro
import { FORMAS_AHORRO_ROUTES } from './formas-ahorro/formas-ahorro.routes';

//Informes — Saldos a fecha de corte
import { SALDOS_CORTE_ROUTES } from './informes/saldos-corte/saldos-corte.routes';

//Informes — Cuentas Nuevas o Retiradas
import { cuentasNRRoutes } from './informes/cuentas-nr/cuentas-nr.routes';

//Informes — Rangos
import { RANGOS_ROUTES } from './informes/rangos/rangos.routes';

//Informes — Inconsistencias
import { INCONSISTENCIAS_ROUTES } from './informes/inconsistencias/inconsistencias.routes';

//Proceso — Habilidad del Asociado
import { habilidadAsociadoRoutes } from './procesos/habilidad-asociado/habilidad-asociado.routes';

// ⭐ Proceso — Crud cuentas de ahorro
import { CUENTAS_AHORRO_ROUTES } from './cuentas-ahorro/cuentas-ahorro.routes';

import { CIERRE_MENSUAL_DEPOSITOS_ROUTES } from './cierre-mensual-depositos/cierre-mensual-depositos.routes';

import { ENTRADAS_SALIDAS_ROUTES } from './informes/entradas-salidas/entradas-salidas.routes';

import { MOVIMIENTOS_DIARIOS_ROUTES } from './informes/movimientos-diarios/movimientos-diarios.routes';

import { RESUMEN_TIPO_MOVIMIENTO_ROUTES } from './informes/resumen-tipo-movimiento/resumen-tipo-movimiento.routes';

import { EXTRACTO_POR_VALOR_ROUTES } from './informes/extracto-por-valor/extracto-por-valor.routes';

import { INTERESES_RETENCION_ROUTES } from './informes/intereses-retencion/intereses-retencion.routes';

import { EXTRACTO_CUENTA_ROUTES } from './informes/extracto-cuenta/extracto-cuenta.routes';

import { EXTRACTO_ASOCIADO_ROUTES } from './informes/extracto-asociado/extracto-asociado.routes';

import { ASOCIADOS_SIN_MOVIMIENTOS_ROUTES } from './informes/asociados-sin-movimiento/asociados-sin-movimientos.routes';

import { MOVIMIENTOS_POR_MESES_ROUTES } from './informes/movimientos-por-meses/movimientos-por-meses.routes';

import { SALDOS_MENORES_ROUTES } from './informes/saldos-menores/saldos-menores.routes';

import { PROMEDIOS_ROUTES } from './informes/promedios/promedios.routes';

import { ANTIGUEDAD_ASOCIADOS_ROUTES } from './informes/antiguedad-asociados/antiguedad-asociados.routes';

import { CUMPLEANIOS_ASOCIADOS_ROUTES } from './informes/cumpleanios-asociados/cumpleanios-asociados.routes';

import { SALDOS_RANGOS_EDAD_ROUTES } from './informes/saldos-rangos-edad/saldos-rangos-edad.routes';

import { ESTADISTICOS_ASOCIADOS_ROUTES } from './informes/estadisticos-asociados/estadisticos-asociados.routes';

import { GMF_SEMANAL_ROUTES } from './informes/gmf-semanal/gmf-semanal.routes';

import { DOCUMENTOS_SOPORTE_ROUTES } from './documentos-soporte/documentos-soporte.routes';

import { DOCUMENTOS_SOPORTE_INFORME_ROUTES } from './informes/documentos-soporte/documentos-soporte.routes';

import { MOVIMIENTO_CUENTA_AHORRO_ROUTES } from './movimientos/cuentas-ahorro/movimiento-cuenta-ahorro.routes';

import { CONCENTRACION_DEPOSITOS_ROUTES } from './analisis/concentracion/concentracion-depositos.routes';

export const DEPOSITOS_ROUTES: Routes = [

  //CONSULTA CUENTAS DE AHORRO

  {
    path: 'cuentas-ahorro',
    children: CUENTAS_AHORRO_ROUTES
  },

  {
    path: 'informes/consulta-cuentas-ahorro',
    children: CONSULTA_CUENTAS_AHORRO_ROUTES
  },

  // 🏛 Formas de ahorro
  {
    path: 'formas-ahorro',
    children: FORMAS_AHORRO_ROUTES
  },

  //Informes — Saldos Corte
  {
    path: 'informes/saldos-corte',
    children: SALDOS_CORTE_ROUTES
  },

  //Informes — Cuentas nuevas/retiradas
  {
    path: 'informes/cuentas-nr',
    children: cuentasNRRoutes
  },

  //Habilidad del Asociado
  {
    path: 'procesos/habilidad-asociado',
    children: habilidadAsociadoRoutes
  },

  //Informes — Rangos
  {
    path: 'informes/rangos',
    children: RANGOS_ROUTES
  },

  //Informes — Inconsistencias
  {
    path: 'informes/inconsistencias',
    children: INCONSISTENCIAS_ROUTES
  },

  //Revalorización
  {
    path: 'procesos/revalorizacion',
    loadChildren: () =>
      import('./procesos/revalorizacion/revalorizacion.routes')
        .then(m => m.revalorizacionRoutes)
  },

  //Interés Diario SM
  {
    path: 'procesos/interes-diario-sm',
    loadChildren: () =>
      import('./procesos/interes-diario-sm/interes-diario-sm.routes')
        .then(m => m.interesDiarioSmRoutes)
  },

  //Interés Mensual SM
  {
    path: 'procesos/interes-mensual-sm',
    loadChildren: () =>
      import('./procesos/interes-mensual-sm/interes-mensual-sm.routes')
        .then(m => m.interesMensualSmRoutes)
  },

  //Interés Mensual TAC
  {
    path: 'procesos/interes-mensual-tac',
    loadChildren: () =>
      import('./procesos/interes-mensual-tac/interes-mensual-tac.routes')
        .then(m => m.interesMensualTacRoutes)
  },

  // Cierre Mensual Depósitos
  {
    path: 'procesos/cierre-mensual-depositos',
    children: CIERRE_MENSUAL_DEPOSITOS_ROUTES
  },

  // Informes — Entradas y salidas
  {
    path: 'informes/entradas-salidas',
    children: ENTRADAS_SALIDAS_ROUTES
  },

  {
    path: 'informes/movimientos-diarios',
    children: MOVIMIENTOS_DIARIOS_ROUTES
  },

  {
    path: 'informes/resumen-tipo-movimiento',
    children: RESUMEN_TIPO_MOVIMIENTO_ROUTES
  },

  {
    path: 'informes/extracto-por-valor',
    children: EXTRACTO_POR_VALOR_ROUTES
  },

  {
    path: 'informes/intereses-retencion',
    children: INTERESES_RETENCION_ROUTES
  },

  {
    path: 'informes/extracto-cuenta',
    children: EXTRACTO_CUENTA_ROUTES
  },

  {
    path: 'informes/extracto-asociado',
    children: EXTRACTO_ASOCIADO_ROUTES
  },

  {
    path: 'informes/asociados-sin-movimientos',
    children: ASOCIADOS_SIN_MOVIMIENTOS_ROUTES
  },

  {
    path: 'informes/movimientos-por-meses',
    children: MOVIMIENTOS_POR_MESES_ROUTES
  },

  {
    path: 'informes/saldos-menores',
    children: SALDOS_MENORES_ROUTES
  },

  {
    path: 'informes/promedios',
    children: PROMEDIOS_ROUTES
  },

  {
    path: 'informes/antiguedad-asociados',
    children: ANTIGUEDAD_ASOCIADOS_ROUTES
  },

  {
    path: 'informes/cumpleanios-asociados',
    children: CUMPLEANIOS_ASOCIADOS_ROUTES
  },

  {
    path: 'informes/saldos-rangos-edad',
    children: SALDOS_RANGOS_EDAD_ROUTES
  },

  {
    path: 'informes/estadisticos-asociados',
    children: ESTADISTICOS_ASOCIADOS_ROUTES
  },

  {
    path: 'cuentas-ahorro/documentos-soporte',
    children: DOCUMENTOS_SOPORTE_ROUTES
  },

  {
    path: 'informes/documentos-soporte',
    children: DOCUMENTOS_SOPORTE_INFORME_ROUTES
  },

  {
    path: 'movimientos/cuentas-ahorro',
    children: MOVIMIENTO_CUENTA_AHORRO_ROUTES
  },

  // Análisis — Concentración de Captaciones
  {
    path: 'analisis/concentracion',
    children: CONCENTRACION_DEPOSITOS_ROUTES
  },

  // Informes — GMF Semanal
  {
    path: 'informes/gmf-semanal',
    children: GMF_SEMANAL_ROUTES
  }

];

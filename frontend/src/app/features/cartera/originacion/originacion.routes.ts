import {
  Routes
} from '@angular/router';

import {
  OriginacionSolicitudComponent
} from './solicitud/originacion-solicitud.component';


export const ORIGINACION_ROUTES:
  Routes = [

  // =========================================================
  // ORIGINACIÓN
  // =========================================================

  {
    path: '',
    component:
      OriginacionSolicitudComponent
  },

  // =========================================================
  // CONTEXTO DEL ASOCIADO
  // =========================================================

  {
    path: 'contexto/:idDatosPersonal',
    loadComponent: () =>
      import(
        './contexto/originacion-contexto.component'
      ).then(
        m => m.OriginacionContextoComponent
      )
  },

  // =========================================================
  // DEUDORES
  // =========================================================

  {
    path: 'deudores',
    loadComponent: () =>
      import(
        './deudores/originacion-deudores.component'
      ).then(
        m => m.OriginacionDeudoresComponent
      )
  },

  // =========================================================
  // BIENES
  // =========================================================

  {
    path: 'bienes',
    loadComponent: () =>
      import(
        './bienes/originacion-bienes.component'
      ).then(
        m => m.OriginacionBienesComponent
      )
  },

  // =========================================================
  // FINANCIERO
  // =========================================================

  {
    path: 'financiero',
    loadComponent: () =>
      import(
        './financiero/originacion-financiero.component'
      ).then(
        m => m.OriginacionFinancieroComponent
      )
  },

  // =========================================================
  // CENTRAL DE RIESGO
  // =========================================================

  {
    path: 'central-riesgo',
    loadComponent: () =>
      import(
        './central-riesgo/originacion-central-riesgo.component'
      ).then(
        m => m.OriginacionCentralRiesgoComponent
      )
  },

  // =========================================================
  // ANÁLISIS
  // =========================================================

  {
    path: 'analisis',
    loadComponent: () =>
      import(
        './analisis/originacion-analisis.component'
      ).then(
        m => m.OriginacionAnalisisComponent
      )
  }

];

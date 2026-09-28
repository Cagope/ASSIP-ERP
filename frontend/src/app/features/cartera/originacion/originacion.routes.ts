import {
  Routes
} from '@angular/router';


export const ORIGINACION_ROUTES:
  Routes = [

  // =========================================================
  // BANDEJA DE SOLICITUDES
  // =========================================================

  {
    path: '',
    loadComponent: () =>
      import(
        './solicitud/list/solicitud-list.component'
      ).then(
        m => m.SolicitudListComponent
      )
  },

  // =========================================================
  // SOLICITUD
  // =========================================================

  {
    path: 'solicitud',
    loadComponent: () =>
      import(
        './solicitud/originacion-solicitud.component'
      ).then(
        m => m.OriginacionSolicitudComponent
      )
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
  },

  // =========================================================
  // REFERENCIAS PERSONALES
  // =========================================================

  {
    path: 'referencias',
    loadComponent: () =>
      import(
        './referencias/originacion-referencias.component'
      ).then(
        m => m.OriginacionReferenciasComponent
      )
  },

  // =========================================================
  // APROBACIÓN DE CARTERA
  // =========================================================

  {
    path: 'aprobacion',
    loadComponent: () =>
      import(
        './aprobacion/originacion-aprobacion.component'
      ).then(
        m => m.OriginacionAprobacionComponent
      )
  },

  // =========================================================
  // FORMALIZACIÓN DE CARTERA - ENTRADA DESDE EL MENÚ
  // =========================================================

  {
    path: 'formalizacion',
    loadComponent: () =>
      import(
        './formalizacion/list/originacion-formalizacion-list.component'
      ).then(
        m => m.OriginacionFormalizacionListComponent
      )
  },

  // =========================================================
  // FORMALIZACIÓN DE CARTERA - SOLICITUD SELECCIONADA
  // =========================================================

  {
    path: 'formalizacion/:idSolicitudCredito',
    loadComponent: () =>
      import(
        './formalizacion/originacion-formalizacion.component'
      ).then(
        m => m.OriginacionFormalizacionComponent
      )
  },

  // =========================================================
  // DESEMBOLSO DE CARTERA - BANDEJA
  // =========================================================

  {
    path: 'desembolso',
    loadComponent: () =>
      import(
        './desembolso/list/originacion-desembolso-list.component'
      ).then(
        m => m.OriginacionDesembolsoListComponent
      )
  },

  // =========================================================
  // DESEMBOLSO DE CARTERA - EJECUCIÓN
  // =========================================================

  {
    path: 'desembolso/:idCarteraCredito',
    loadComponent: () =>
      import(
        './desembolso/originacion-desembolso.component'
      ).then(
        m => m.OriginacionDesembolsoComponent
      )
  },

  // =========================================================
  // IMPRESIÓN DEL PAGARÉ DILIGENCIADO
  // =========================================================

  {
    path: 'impresion/pagare-diligenciado/:idSolicitudCredito',
    loadComponent: () =>
      import(
        './impresion/pagare-diligenciado/pagare-diligenciado.component'
      ).then(
        m => m.PagareDiligenciadoComponent
      )
  },

  // =========================================================
  // IMPRESIÓN DEL PAGARÉ EN BLANCO
  // =========================================================

  {
    path: 'impresion/pagare-en-blanco/:idSolicitudCredito',
    loadComponent: () =>
      import(
        './impresion/pagare-en-blanco/pagare-en-blanco.component'
      ).then(
        m => m.PagareEnBlancoComponent
      )
  },

  // =========================================================
  // CARTA DE INSTRUCCIONES
  // =========================================================

  {
    path: 'impresion/carta-instrucciones/:idSolicitudCredito',
    loadComponent: () =>
      import(
        './impresion/carta-instrucciones/carta-instrucciones.component'
      ).then(
        m => m.CartaInstruccionesComponent
      )
  }

];

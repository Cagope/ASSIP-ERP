import {
  Routes
} from '@angular/router';

import {
  permisoGuard
} from '../../../core/auth/permiso.guard';


export const ORIGINACION_ROUTES:
  Routes = [

  // =========================================================
  // BANDEJA DE SOLICITUDES
  // =========================================================

  {
    path: '',
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
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
    canActivate: [permisoGuard],
    data: {
      permiso: 'CARTERA_ORIGINACION_VIEW'
    },
    loadComponent: () =>
      import(
        './impresion/carta-instrucciones/carta-instrucciones.component'
      ).then(
        m => m.CartaInstruccionesComponent
      )
  }

];

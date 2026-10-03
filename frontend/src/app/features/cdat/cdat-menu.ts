export const cdatMenu = {
  title: 'CDAT',
  icon: 'landmark',

  items: [

    // =============================
    // OPERACIÓN
    // =============================
    {
      label: 'Operación',
      children: [

        {
          label: 'Inclusión de CDAT',
          route: '/cdat/cdats',
          permiso: 'CDAT_APERTURA_CREATE'
        },

        {
          label: 'Cancelación / Renovación',
          route: '/cdat/cancelacion',
          permiso: 'CDAT_CANCELACION_EXECUTE'
        }

      ]
    },

    // =============================
    // PROCESOS
    // =============================
    {
      label: 'Procesos',
      children: [

        {
          label: 'Liquidación diaria',
          route: '/cdat/liquidacion-diaria',
          permiso: 'CDAT_LIQUIDACION_DIARIA_EXECUTE'
        },

        {
          label: 'Cierre mensual CDAT',
          route: '/cdat/cierre-mensual-cdat',
          permiso: 'CDAT_CIERRE_MENSUAL_EXECUTE'
        },

        {
          label: 'Causación mensual CDAT',
          route: '/cdat/causacion-mensual-cdat',
          permiso: 'CDAT_CAUSACION_MENSUAL_EXECUTE'
        }

      ]
    },

    // =============================
    // ANÁLISIS
    // =============================
    {
      label: 'Análisis',
      children: [

        {
          label: 'Concentración CDAT',
          route: '/cdat/analisis/concentracion',
          permiso: 'CDAT_ANALISIS_VIEW'
        },

        {
          label: 'Tasas y condiciones CDAT',
          route: '/cdat/analisis/tasas-condiciones',
          permiso: 'CDAT_ANALISIS_VIEW'
        }

      ]
    },

    // =============================
    // INFORMES
    // =============================
    {
      label: 'Informes',
      children: [

        {
          label: 'Consulta de CDATs',
          route: '/cdat/informes/consulta-cdats',
          permiso: 'CDAT_INFORMES_VIEW'
        },

        {
          label: 'Simulador CDAT',
          route: '/cdat/informes/simulador-cdat',
          permiso: 'CDAT_INFORMES_VIEW'
        },

        {
          label: 'Estadísticos CDAT',
          route: '/cdat/informes/estadisticos-cdat',
          permiso: 'CDAT_INFORMES_VIEW'
        },

        {
          label: 'Informes por fechas CDAT',
          route: '/cdat/informes/fechas-cdat',
          permiso: 'CDAT_INFORMES_VIEW'
        }

      ]
    }

  ]
};

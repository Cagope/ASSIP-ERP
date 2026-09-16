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
          route: '/cdat/cdats'
        },

        {
          label: 'Cancelación / Renovación',
          route: '/cdat/cancelacion'
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
          route: '/cdat/liquidacion-diaria'
        },

        {
          label: 'Cierre mensual CDAT',
          route: '/cdat/cierre-mensual-cdat'
        },

        {
          label: 'Causación mensual CDAT',
          route: '/cdat/causacion-mensual-cdat'
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
          route: '/cdat/analisis/concentracion'
        },

        {
          label: 'Tasas y condiciones CDAT',
          route: '/cdat/analisis/tasas-condiciones'
        }

      ]
    },

    // =============================
    // CONSULTAS / INFORMES
    // =============================
    {
      label: 'Informes',
      children: [

        {
          label: 'Consulta de CDATs',
          route: '/cdat/informes/consulta-cdats'
        },

        {
          label: 'Simulador CDAT',
          route: '/cdat/informes/simulador-cdat'
        },

        {
          label: 'Estadísticos CDAT',
          route: '/cdat/informes/estadisticos-cdat'
        },

        {
          label: 'Informes por fechas CDAT',
          route: '/cdat/informes/fechas-cdat'
        }

      ]
    }

  ]
};

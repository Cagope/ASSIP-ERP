export const cdatMenu = {
  title: 'CDAT',
  icon: 'landmark',

  items: [

    {
      label: 'Operación',
      children: [
        {
          label: 'Inclusión de CDAT',
          route: '/cdat/cdats'
        }
      ]
    },

    {
      label: 'Procesos',
      children: [
        {
          label: 'Liquidación diaria',
          route: '/cdat/procesos/liquidacion-diaria'
        },
        {
          label: 'Cancelación / Renovación',
          route: '/cdat/procesos/cancelacion-renovacion'
        }
      ]
    },

    {
      label: 'Informes',
      children: [
        {
          label: 'Consulta de CDAT',
          route: '/cdat/informes/consulta-cdats'
        },
        {
          label: 'Extractos CDAT',
          route: '/cdat/informes/extractos'
        },
        {
          label: 'Vencimientos',
          route: '/cdat/informes/vencimientos'
        }
      ]
    }

  ]
};

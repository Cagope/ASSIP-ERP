export const GERENCIA_MENU = {
  title: 'Gerencia',

  items: [

    {
      label: 'Dashboards',
      children: [

        {
          label: 'Dashboard CDAT',
          route: '/gerencia/dashboard-cdat'
        },

        {
          label: 'Dashboard Depósitos',
          route: '/gerencia/dashboard-depositos'
        },

        {
          label: 'Dashboard Créditos',
          route: '/gerencia/dashboard-creditos'
        },

        {
          label: 'Dashboard General',
          route: '/gerencia/dashboard-general'
        }

      ]
    },

    {
      label: 'Expediente del Asociado',
      children: [

        {
          label: 'Consulta Integral',
          route: '/gerencia/expediente-asociado'
        }

      ]
    },

    {
      label: 'Indicadores',
      children: [

        {
          label: 'Captación por agencias',
          route: '/gerencia/captacion-agencias'
        }

      ]
    }

  ]
};

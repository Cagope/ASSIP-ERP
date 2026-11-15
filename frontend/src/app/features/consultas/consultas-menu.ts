/* =========================================================
   ASSIP ERP - GESTIÓN GERENCIAL
   Menú de consultas y dashboards institucionales
   COOPVALLE
   ========================================================= */

export const consultasMenu = {
  title: 'Gestión Gerencial',

  items: [

    // =====================================================
    // CONSULTAS
    // =====================================================
    {
      label: 'Consultas',
      children: [
        {
          label: 'Expediente Integral del Asociado',
          route: '/consultas/expediente-asociado',
          permiso: 'EXPEDIENTE_ASOCIADO_VIEW'
        }
      ]
    },

    // =====================================================
    // DASHBOARDS GERENCIALES
    // =====================================================
    {
      label: 'Dashboards',
      children: [

        {
          label: 'Dashboard de Depósitos',
          route: '/depositos/analisis/dashboard-depositos',
          permiso: 'DEPOSITOS_DASHBOARD_VIEW'
        },

        {
          label: 'Dashboard de Cartera',
          route: '/cartera/analisis/dashboard-cartera',
          permiso: 'CARTERA_DASHBOARD_VIEW'
        },

        {
          label: 'Dashboard de CDAT',
          route: '/cdat/analisis/dashboard-cdat',
          permiso: 'CDAT_DASHBOARD_VIEW'
        }

      ]
    }

  ]
};

// ================================================
// Menú — Superintendencia (SES)
// ================================================

export const sesMenu = {
  title: 'Superintendencia',
  items: [

    // ============================================
    // FORMATO 3 - SALDOS CONTABLES
    // ============================================
    {
      label: 'Formato 3 - Saldos Contables',
      route: '/ses/contabilidad/formato-3',
      permiso: 'SES_FORMATOS_VIEW'
    },

    // ============================================
    // FORMATO 9999 - ASOCIADOS
    // ============================================
    {
      label: 'Formato 9999 - Asociados',
      route: '/ses/asociados',
      permiso: 'SES_FORMATOS_VIEW'
    },

    // ============================================
    // FORMATO 9013 - APORTES SOCIALES
    // ============================================
    {
      label: 'Formato 9013 - Aportes Sociales',
      route: '/ses/aportes',
      permiso: 'SES_FORMATOS_VIEW'
    },

    // ============================================
    // FORMATO 9026 - AHORROS
    // Depósitos y CDAT
    // ============================================
    {
      label: 'Formato 9026 - Ahorros',
      route: '/ses/ahorros',
      permiso: 'SES_FORMATOS_VIEW'
    },

    // FORMATO 9027 - CARTERA INDIVIDUAL
    {
      label: 'Formato 9027 - Cartera Individual',
      route: '/ses/cartera',
      permiso: 'SES_FORMATOS_VIEW'
    }

  ]
};

export const nominaMenu = {
  title: '💼 Nómina',
  items: [

    // =========================
    // 📚 CATÁLOGOS
    // =========================

    {
      label: 'Cargos',
      route: '/nomina/cargos',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Secciones',
      route: '/nomina/secciones',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'EPS',
      route: '/nomina/eps',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'AFP',
      route: '/nomina/afp',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'ARL',
      route: '/nomina/arl',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Caja Compensación',
      route: '/nomina/caja-compensacion',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Variables Vigencia',
      route: '/nomina/variables-vigencia',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Conceptos Nómina',
      route: '/nomina/conceptos-nomina',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // 👤 OPERACIÓN
    // =========================

    {
      label: 'Empleados',
      route: '/nomina/empleados',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Cesantías',
      route: '/nomina/cesantias',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Contratos',
      route: '/nomina/empleado-contratos',
      permiso: 'NOMINA_VIEW'
    }

  ]
};

export const nominaMenu = {
  title: '💼 Nómina',
  items: [

    // =========================
    // 🏗 ESTRUCTURA HUMANA
    // =========================

    {
      label: 'Empleados',
      route: '/nomina/empleados',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Contratos',
      route: '/nomina/empleado-contratos',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // 📅 CALENDARIO DE NÓMINA
    // =========================

    {
      label: 'Períodos Nómina',
      route: '/nomina/periodos',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Generar Períodos',
      route: '/nomina/periodos/generador',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // ⚙ OPERACIÓN
    // =========================

    {
      label: 'Novedades Nómina',
      route: '/nomina/novedades',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Novedades Masivas',
      route: '/nomina/novedades/masivo',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // 🧮 LIQUIDACIÓN DE NÓMINA
    // =========================

    {
      label: 'Liquidación Nómina',
      route: '/nomina/liquidacion',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Cesantías',
      route: '/nomina/cesantias',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // 🧾 CONTABILIZACIÓN
    // =========================

    {
      label: 'Contabilización Liquidación',
      route: '/nomina/contabilizacion/liquidacion',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Contabilización SGSSI y Parafiscales',
      route: '/nomina/contabilizacion/aportes-parafiscales',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Contabilización Prestaciones Sociales',
      route: '/nomina/contabilizacion/prestaciones-sociales',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // 📚 CONFIGURACIÓN
    // =========================

    {
      label: 'Conceptos Nómina',
      route: '/nomina/conceptos-nomina',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Conceptos ↔ Cuentas Contables',
      route: '/nomina/concepto-cuentas-contables',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Variables Vigencia',
      route: '/nomina/variables-vigencia',
      permiso: 'NOMINA_VIEW'
    },

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
    }

  ]
};

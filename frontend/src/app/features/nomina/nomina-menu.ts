export const nominaMenu = {
  title: '💼 Nómina',
  items: [

    // =========================
    // 👥 PERSONAL Y CONTRATOS
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
    // 📅 PERÍODOS DE NÓMINA
    // =========================
    {
      label: 'Períodos',
      route: '/nomina/periodos',
      permiso: 'NOMINA_VIEW'
    },
    {
      label: 'Generar Períodos',
      route: '/nomina/periodos/generador',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // 🧾 NOVEDADES Y EVENTOS
    // =========================
    {
      label: 'Novedades',
      route: '/nomina/novedades',
      permiso: 'NOMINA_VIEW'
    },
    {
      label: 'Novedades Masivas',
      route: '/nomina/novedades/masivo',
      permiso: 'NOMINA_VIEW'
    },
    {
      label: 'Eventos de Liquidación',
      route: '/nomina/eventos-liquidacion',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // 🧮 PROCESOS DE NÓMINA
    // =========================
    {
      label: 'Liquidación (V1)',
      route: '/nomina/liquidacion',
      permiso: 'NOMINA_VIEW'
    },
    {
      label: 'Liquidación (V2)',
      route: '/nomina/liquidacion-v2',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // 🧾 CONTABILIZACIÓN
    // =========================
    {
      label: 'Contabilizar Liquidación',
      route: '/nomina/contabilizacion/liquidacion',
      permiso: 'NOMINA_VIEW'
    },
    {
      label: 'Contabilizar Seguridad Social',
      route: '/nomina/contabilizacion/aportes-parafiscales',
      permiso: 'NOMINA_VIEW'
    },
    {
      label: 'Contabilizar Prestaciones',
      route: '/nomina/contabilizacion/prestaciones-sociales',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // ⚙ CONFIGURACIÓN DE NÓMINA
    // =========================
    {
      label: 'Conceptos de Nómina',
      route: '/nomina/conceptos-nomina',
      permiso: 'NOMINA_VIEW'
    },
    {
      label: 'Cuentas por Concepto',
      route: '/nomina/concepto-cuentas-contables',
      permiso: 'NOMINA_VIEW'
    },
    {
      label: 'Parámetros (Vigencias)',
      route: '/nomina/variables-vigencia',
      permiso: 'NOMINA_VIEW'
    },

    // =========================
    // 🏢 ENTIDADES Y CATÁLOGOS
    // =========================
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
      label: 'Caja de Compensación',
      route: '/nomina/caja-compensacion',
      permiso: 'NOMINA_VIEW'
    },
    {
      label: 'Fondos de Cesantías',
      route: '/nomina/cesantias',
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

    // =========================
    // 📊 INFORMES
    // =========================
    {
      label: 'Informe novedades por empleado',
      route: '/nomina/informes/novedades-empleado',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Informe novedades por concepto',
      route: '/nomina/informes/novedades-concepto',
      permiso: 'NOMINA_VIEW'
    },

    {
      label: 'Informe Consolidado por conceptos',
      route: '/nomina/informes/consolidado-conceptos',
      permiso: 'NOMINA_VIEW'
    }

  ]
};

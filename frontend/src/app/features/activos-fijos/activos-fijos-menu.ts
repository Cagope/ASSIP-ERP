export const activosFijosMenu = {
  title: '🏗 Activos Fijos',
  items: [

    // ======================================================
    // 🔧 MAESTROS
    // ======================================================
    {
      label: 'Localizaciones',
      route: '/activos-fijos/localizaciones',
      permiso: 'ACTIVOS_FIJOS_VIEW'
    },
    {
      label: 'Bloques',
      route: '/activos-fijos/bloques',
      permiso: 'ACTIVOS_FIJOS_VIEW'
    },

    // ======================================================
    // 🧾 OPERACIÓN
    // ======================================================
    {
      label: 'Activos fijos',
      route: '/activos-fijos/activos',
      permiso: 'ACTIVOS_FIJOS_VIEW'
    },
    {
      label: 'Ingreso de activos',
      route: '/activos-fijos/ingreso',
      permiso: 'ACTIVOS_FIJOS_INGRESO'
    },

    // ======================================================
    // ⚙️ PROCESOS
    // ======================================================
    {
      label: 'Depreciación mensual',
      route: '/activos-fijos/depreciacion',
      permiso: 'ACTIVOS_FIJOS_DEPRECIACION'
    },

    // ======================================================
    // 📊 INFORMES
    // ======================================================
    {
      label: 'Maestro de activos',
      route: '/activos-fijos/informes/maestro-activos',
      permiso: 'ACTIVOS_FIJOS_VIEW'
    },
    {
      label: 'Movimientos de activos',
      route: '/activos-fijos/informes/movimientos-activos',
      permiso: 'ACTIVOS_FIJOS_VIEW'
    },
    {
      label: 'Kardex por activo',
      route: '/activos-fijos/informes/kardex-activo',
      permiso: 'ACTIVOS_FIJOS_VIEW'
    },
    {
      label: 'Resumen mensual',
      route: '/activos-fijos/informes/resumen-movimientos',
      permiso: 'ACTIVOS_FIJOS_VIEW'
    },
    {
      label: 'Depreciación (entre fechas)',
      route: '/activos-fijos/informes/depreciacion',
      permiso: 'ACTIVOS_FIJOS_VIEW'
    }
  ]
};

export const activosFijosMenu = {
  title: 'Activos Fijos',
  items: [

    {
      label: 'Maestros',
      children: [
        {
          label: 'Localizaciones',
          route: '/activos-fijos/localizaciones',
          permiso: 'ACTIVOS_FIJOS_VIEW'
        },
        {
          label: 'Bloques',
          route: '/activos-fijos/bloques',
          permiso: 'ACTIVOS_FIJOS_VIEW'
        }
      ]
    },

    {
      label: 'Operación',
      children: [
        {
          label: 'Activos fijos',
          route: '/activos-fijos/activos',
          permiso: 'ACTIVOS_FIJOS_VIEW'
        },
        {
          label: 'Ingreso de activos',
          route: '/activos-fijos/ingreso',
          permiso: 'ACTIVOS_FIJOS_INGRESO'
        }
      ]
    },

    {
      label: 'Procesos',
      children: [
        {
          label: 'Depreciación mensual',
          route: '/activos-fijos/depreciacion',
          permiso: 'ACTIVOS_FIJOS_DEPRECIACION'
        }
      ]
    },

    {
      label: 'Informes',
      children: [
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
          label: 'Depreciación entre fechas',
          route: '/activos-fijos/informes/depreciacion',
          permiso: 'ACTIVOS_FIJOS_VIEW'
        }
      ]
    }

  ]
};

export const cajasMenu = {
  title: 'Caja',
  icon: 'wallet',

  items: [

    // =============================
    // OPERACIÓN
    // =============================
    {
      label: 'Operación',
      children: [

        {
          label: 'Inicio de caja',
          route: '/cajas/provisiones',
          permiso: 'CAJAS_VINCULAR_CAJA'
        },

        {
          label: 'Caja / Ventanilla',
          route: '/cajas/ventanilla'
        },

        {
          label: 'Operaciones pendientes',
          route: '/cajas/operaciones-pendientes',
          permiso: 'CAJAS_OPERACIONES_PENDIENTES_VIEW'
        },

        {
          label: 'Captura depósitos',
          route: '/cajas/captura_depositos'
        },

        {
          label: 'Recaudos convenios',
          route: '/cajas/recaudos-convenios'
        },

        {
          label: 'Movimientos interagencia',
          route: '/cajas/movimientos-interagencia'
        },

        {
          label: 'Cierre recaudos convenios',
          route: '/cajas/cierre-recaudos-convenios'
        },

        {
          label: 'Cierre de caja',
          route: '/cajas/cierres'
        }

      ]
    },

    // =============================
    // PARAMETRIZACIÓN
    // =============================
    {
      label: 'Parametrización',
      children: [

        {
          label: 'Convenios de recaudo',
          route: '/cajas/convenios-recaudo'
        }

      ]
    }

  ]
};

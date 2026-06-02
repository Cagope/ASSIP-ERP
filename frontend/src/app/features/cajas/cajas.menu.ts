export const cajasMenu = {
  title: 'Cajas',
  icon: 'wallet',

  items: [

    // =============================
    // OPERACIÓN
    // =============================
    {
      label: 'Operación',
      children: [

        {
          label: 'Vincular caja',
          route: '/cajas/vincular-caja'
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

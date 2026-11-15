
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
          route: '/cajas/ventanilla',
          permiso: 'CAJAS_VENTANILLA_VIEW'
        },

        {
          label: 'Salida de cheques',
          route: '/cajas/salida-cheques',
          permiso: 'CAJAS_SALIDA_CHEQUES_EJECUTAR'
        },

        {
          label: 'Operaciones pendientes',
          route: '/cajas/operaciones-pendientes',
          permiso: 'CAJAS_OPERACIONES_PENDIENTES_VIEW'
        },

        {
          label: 'Captura depósitos',
          route: '/cajas/captura_depositos',
          permiso: 'CAJAS_CAPTURA_DEPOSITOS_VIEW'
        },

        {
          label: 'Recaudos convenios',
          route: '/cajas/recaudos-convenios',
          permiso: 'CAJAS_RECAUDOS_CONVENIOS_VIEW'
        },

        {
          label: 'Movimientos interagencia',
          route: '/cajas/movimientos-interagencia',
          permiso: 'CAJAS_MOVIMIENTOS_INTERAGENCIA_VIEW'
        },

        {
          label: 'Cierre recaudos convenios',
          route: '/cajas/cierre-recaudos-convenios',
          permiso: 'CAJAS_CIERRE_RECAUDOS_CONVENIOS_EXECUTE'
        },

        {
          label: 'Cierre de caja',
          route: '/cajas/cierres',
          permiso: 'CAJAS_CIERRE_EXECUTE'
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
          route: '/cajas/convenios-recaudo',
          permiso: 'CAJAS_CONVENIOS_RECAUDO_VIEW'
        }

      ]
    }

  ]
};

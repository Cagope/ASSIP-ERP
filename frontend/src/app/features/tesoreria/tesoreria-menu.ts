
export const tesoreriaMenu = {
  title: 'Tesorería',
  icon: 'wallet',

  items: [

    // =========================================================
    // OPERACIÓN
    // =========================================================
    {
      label: 'Operación',
      children: [

        // -----------------------------------------------------
        // MOVIMIENTOS DE FONDOS
        // Consignaciones, retiros y traslados
        // -----------------------------------------------------
        {
          label: 'Movimientos de Fondos',
          route: '/tesoreria/movimientos-fondos',
          permiso: 'TESORERIA_MOVIMIENTOS_FONDOS_VIEW'
        },

        // -----------------------------------------------------
        // OPERACIONES PENDIENTES
        // Obligaciones y pagos de Tesorería
        // -----------------------------------------------------
        {
          label: 'Operaciones Pendientes',
          route: '/tesoreria/operaciones-pendientes',
          permiso: 'TESORERIA_MOVIMIENTOS_FONDOS_VIEW'
        },

        // -----------------------------------------------------
        // AUTORIZACIÓN DE COMPROBANTES
        // Consulta, autorización, rechazo y anulación
        // -----------------------------------------------------
        {
          label: 'Autorización de Comprobantes',
          route: '/tesoreria/autorizacion-comprobantes',
          permiso: 'TESORERIA_AUTORIZACION_COMPROBANTES_VIEW'
        }

      ]
    }

  ]
};

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

        {
          label: 'Movimientos de Fondos',
          route: '/tesoreria/movimientos-fondos',
          permiso: 'TESORERIA_MOVIMIENTOS_FONDOS_VIEW'
        }

      ]
    }

  ]
};

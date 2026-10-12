export const tesoreriaMenu = {
  title: 'Tesorería',
  icon: 'wallet',

  items: [

    // =============================
    // OPERACIÓN
    // =============================
    {
      label: 'Operación',
      children: [

        {
          label: 'Consignaciones bancarias',
          route: '/tesoreria/consignaciones-bancarias',
          permiso: 'TESORERIA_CONSIGNACIONES_BANCARIAS_VIEW'
        }

      ]
    }

  ]
};

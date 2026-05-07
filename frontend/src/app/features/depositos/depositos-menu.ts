export const depositosMenu = {
  title: 'Depósitos',
  items: [

    {
      label: 'Operación',
      children: [
        {
          label: 'Actualización de cuentas',
          route: '/depositos/cuentas-ahorro',
          permiso: 'DEPOSITOS_CUENTAS_VIEW'
        }
      ]
    },

    {
      label: 'Procesos',
      children: [
        {
          label: 'Revalorización de aportes',
          route: '/depositos/procesos/revalorizacion'
        },

        {
          label: 'Liquidación de intereses',
          children: [
            {
              label: 'Interés diario SM',
              route: '/depositos/procesos/interes-diario-sm'
            },
            {
              label: 'Interés mensual SM',
              route: '/depositos/procesos/interes-mensual-sm'
            },
            {
              label: 'Interés mensual TAC',
              route: '/depositos/procesos/interes-mensual-tac'
            }
          ]
        },

        {
          label: 'Habilidad del asociado',
          route: '/depositos/procesos/habilidad-asociado'
        }
      ]
    },

    {
      label: 'Informes',
      children: [
        {
          label: 'Consulta cuentas de ahorro',
          route: '/depositos/informes/consulta-cuentas-ahorro'
        },
        {
          label: 'Saldos a corte',
          route: '/depositos/informes/saldos-corte'
        },
        {
          label: 'Cuentas nuevas o retiradas',
          route: '/depositos/informes/cuentas-nr'
        },
        {
          label: 'Informe por rangos',
          route: '/depositos/informes/rangos'
        },
        {
          label: 'Inconsistencias de saldos',
          route: '/depositos/informes/inconsistencias'
        }
      ]
    }

  ]
};

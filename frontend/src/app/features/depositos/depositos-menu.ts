export const depositosMenu = {
  title: 'Depósitos',
  items: [

    // ======================================================
    // OPERACIÓN
    // ======================================================
    {
      label: 'Operación',
      children: [

        {
          label: 'Actualización de cuentas',
          route: '/depositos/cuentas-ahorro',
          permiso: 'DEPOSITOS_CUENTAS_VIEW'
        },

        {
          label: 'Documentos soporte',
          route: '/depositos/cuentas-ahorro/documentos-soporte',
          permiso: 'DEPOSITOS_CUENTAS_VIEW'
        }

      ]
    },

    // ======================================================
    // PROCESOS
    // ======================================================
    {
      label: 'Procesos',
      children: [

        {
          label: 'Movimientos cuentas ahorro',
          route: '/depositos/movimientos/cuentas-ahorro'
        },

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
        },

        {
          label: 'Cierre mensual depósitos',
          route: '/depositos/procesos/cierre-mensual-depositos'
        }

      ]
    },

    // ======================================================
    // ANÁLISIS
    // ======================================================
    {
      label: 'Análisis',
      children: [

        {
          label: 'Concentración de captaciones',
          route: '/depositos/analisis/concentracion'
        }

      ]
    },


    // ======================================================
    // INFORMES
    // ======================================================
    {
      label: 'Informes',
      children: [

        // ----------------------------------------------
        // CONSULTAS GENERALES
        // ----------------------------------------------
        {
          label: 'Consultas',
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
              label: 'Extracto de cuenta',
              route: '/depositos/informes/extracto-cuenta'
            }

          ]
        },

        // ----------------------------------------------
        // MOVIMIENTOS
        // ----------------------------------------------
        {
          label: 'Movimientos',
          children: [

            {
              label: 'Entradas y salidas',
              route: '/depositos/informes/entradas-salidas'
            },

            {
              label: 'Movimientos por meses',
              route: '/depositos/informes/movimientos-por-meses'
            },

            {
              label: 'Movimientos diarios',
              route: '/depositos/informes/movimientos-diarios'
            },

            {
              label: 'Resumen tipo movimiento',
              route: '/depositos/informes/resumen-tipo-movimiento'
            },

            {
              label: 'Extracto por valor',
              route: '/depositos/informes/extracto-por-valor'
            },

            {
              label: 'Extracto asociado',
              route: '/depositos/informes/extracto-asociado'
            },

            {
              label: 'Intereses y retención',
              route: '/depositos/informes/intereses-retencion'
            },
            {
              label: 'Asociados sin movimientos',
              route: '/depositos/informes/asociados-sin-movimientos'
            }

          ]
        },

        // ----------------------------------------------
        // CONTROL
        // ----------------------------------------------
        {
          label: 'Control',
          children: [

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
            },

            {
              label: 'Saldos menores',
              route: '/depositos/informes/saldos-menores'
            },

            {
              label: 'De promedios',
              route: '/depositos/informes/promedios'
            },

            {
              label: 'Antigüedad de asociados',
              route: '/depositos/informes/antiguedad-asociados'
            },

            {
              label: 'Cumpleaños de asociados',
              route: '/depositos/informes/cumpleanios-asociados'
            },

            {
              label: 'Saldos por rangos de edad',
              route: '/depositos/informes/saldos-rangos-edad'
            },

            {
              label: 'Estadísticos de asociados',
              route: '/depositos/informes/estadisticos-asociados'
            },

            {
              label: 'Documentos soporte',
              route: '/depositos/informes/documentos-soporte'
            },

            {
              label: 'GMF semanal',
              route: '/depositos/informes/gmf-semanal'
            }

          ]
        }

      ]
    }

  ]
};

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
          permiso: 'DEPOSITOS_DOCUMENTOS_SOPORTE_VIEW'
        },

        {
          label: 'Formas de ahorro',
          route: '/depositos/formas-ahorro',
          permiso: 'DEPOSITOS_FORMAS_AHORRO_VIEW'
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
          route: '/depositos/movimientos/cuentas-ahorro',
          permiso: 'DEPOSITOS_MOVIMIENTOS_VIEW'
        },

        {
          label: 'Revalorización de aportes',
          route: '/depositos/procesos/revalorizacion',
          permiso: 'DEPOSITOS_REVALORIZACION_EXECUTE'
        },

        {
          label: 'Liquidación de intereses',
          children: [

            {
              label: 'Interés diario SM',
              route: '/depositos/procesos/interes-diario-sm',
              permiso: 'DEPOSITOS_INTERESES_EXECUTE'
            },

            {
              label: 'Interés mensual SM',
              route: '/depositos/procesos/interes-mensual-sm',
              permiso: 'DEPOSITOS_INTERESES_EXECUTE'
            },

            {
              label: 'Interés mensual TAC',
              route: '/depositos/procesos/interes-mensual-tac',
              permiso: 'DEPOSITOS_INTERESES_EXECUTE'
            }

          ]
        },

        {
          label: 'Habilidad del asociado',
          route: '/depositos/procesos/habilidad-asociado',
          permiso: 'DEPOSITOS_HABILIDAD_ASOCIADO_EXECUTE'
        },

        {
          label: 'Cierre mensual depósitos',
          route: '/depositos/procesos/cierre-mensual-depositos',
          permiso: 'DEPOSITOS_CIERRE_MENSUAL_EXECUTE'
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
          route: '/depositos/analisis/concentracion',
          permiso: 'DEPOSITOS_ANALISIS_VIEW'
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
              route: '/depositos/informes/consulta-cuentas-ahorro',
              permiso: 'DEPOSITOS_CONSULTA_CUENTAS_VIEW'
            },

            {
              label: 'Saldos a corte',
              route: '/depositos/informes/saldos-corte',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Extracto de cuenta',
              route: '/depositos/informes/extracto-cuenta',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
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
              route: '/depositos/informes/entradas-salidas',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Movimientos por meses',
              route: '/depositos/informes/movimientos-por-meses',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Movimientos diarios',
              route: '/depositos/informes/movimientos-diarios',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Resumen tipo movimiento',
              route: '/depositos/informes/resumen-tipo-movimiento',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Extracto por valor',
              route: '/depositos/informes/extracto-por-valor',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Extracto asociado',
              route: '/depositos/informes/extracto-asociado',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Intereses y retención',
              route: '/depositos/informes/intereses-retencion',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Asociados sin movimientos',
              route: '/depositos/informes/asociados-sin-movimientos',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
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
              route: '/depositos/informes/cuentas-nr',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Informe por rangos',
              route: '/depositos/informes/rangos',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Inconsistencias de saldos',
              route: '/depositos/informes/inconsistencias',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Saldos menores',
              route: '/depositos/informes/saldos-menores',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'De promedios',
              route: '/depositos/informes/promedios',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Antigüedad de asociados',
              route: '/depositos/informes/antiguedad-asociados',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Cumpleaños de asociados',
              route: '/depositos/informes/cumpleanios-asociados',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Saldos por rangos de edad',
              route: '/depositos/informes/saldos-rangos-edad',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Estadísticos de asociados',
              route: '/depositos/informes/estadisticos-asociados',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'Documentos soporte',
              route: '/depositos/informes/documentos-soporte',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            },

            {
              label: 'GMF semanal',
              route: '/depositos/informes/gmf-semanal',
              permiso: 'DEPOSITOS_INFORMES_VIEW'
            }

          ]
        }

      ]
    }

  ]
};

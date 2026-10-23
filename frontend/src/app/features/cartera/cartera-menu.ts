export const carteraMenu = {
  title: 'Cartera',
  icon: 'cartera',

  items: [

    // =========================================================
    // OPERACIÓN
    // =========================================================
    {
      label: 'Operación',
      children: [

        // =====================================================
        // FÁBRICA DE CRÉDITOS
        // =====================================================
        {
          label: 'Fábrica de Créditos',
          children: [

            {
              label: 'Originación de Cartera',
              route: '/cartera/originacion',
              permiso: 'CARTERA_ORIGINACION_VIEW'
            },

            {
              label: 'Referencias Personales',
              route: '/cartera/originacion/referencias',
              permiso: 'CARTERA_ORIGINACION_VIEW'
            },

            {
              label: 'Aprobación de Cartera',
              route: '/cartera/originacion/aprobacion',
              permiso: 'CARTERA_ORIGINACION_VIEW'
            },

            {
              label: 'Formalización de Cartera',
              route: '/cartera/originacion/formalizacion',
              permiso: 'CARTERA_ORIGINACION_VIEW'
            },

            {
              label: 'Desembolso de Cartera',
              route: '/cartera/originacion/desembolso',
              permiso: 'CARTERA_ORIGINACION_VIEW'
            }

          ]
        },


        // =====================================================
        // CONSULTA DE CRÉDITOS
        // =====================================================
        {
          label: 'Consulta de Créditos',
          route: '/cartera/consulta-creditos',
          permiso: 'CARTERA_VIEW'
        },


        // =====================================================
        // PAGO DE CUOTAS V1
        // =====================================================
        {
          label: 'Pago de Cuotas',
          route: '/cartera/pago-cuotas',
          permiso: 'CARTERA_PAGO_CUOTAS'
        }

      ]
    },


    // =========================================================
    // PROCESOS
    // =========================================================
    {
      label: 'Procesos',
      children: [

        // =====================================================
        // CIERRE MENSUAL
        // =====================================================
        {
          label: 'Cierre Mensual',
          children: [

            {
              label: 'Consolidación del Cierre',
              route: '/cartera/cierre-mensual',
              permiso: 'CARTERA_CIERRE_VIEW'
            },

            {
              label: 'Cálculos',
              route: '/cartera/cierre-mensual/calculos',
              permiso: 'CARTERA_CIERRE_VIEW'
            },

            {
              label: 'Anexo 1',
              route: '/cartera/cierre-mensual/anexo1',
              permiso: 'CARTERA_CIERRE_VIEW'
            },

            {
              label: 'Anexo PE',
              route: '/cartera/cierre-mensual/anexo2',
              permiso: 'CARTERA_CIERRE_VIEW'
            }

          ]
        },


        // =====================================================
        // EVALUACIÓN DE CARTERA
        // =====================================================
        {
          label: 'Evaluación de Cartera',
          children: [

            {
              label: 'Evaluaciones de Cartera',
              route: '/cartera/evaluacion/evaluaciones',
              permiso: 'CARTERA_EVALUACION_VIEW'
            },

            {
              label: 'Resultados Central de Riesgos',
              route: '/cartera/evaluacion/central-riesgos/resultados',
              permiso: 'CARTERA_EVALUACION_VIEW'
            }

          ]
        }

      ]
    },


    // =========================================================
    // ANÁLISIS DE CARTERA
    // =========================================================
    {
      label: 'Análisis de Cartera',
      children: [

        // =====================================================
        // DASHBOARD DE CARTERA
        // =====================================================
        {
          label: 'Dashboard de Cartera',
          route: '/cartera/analisis/dashboard-cartera',
          permiso: 'CARTERA_DASHBOARD_VIEW'
        },


        // =====================================================
        // VECTOR DE COMPORTAMIENTO
        // =====================================================
        {
          label: 'Vector de Comportamiento',
          children: [

            {
              label: 'Actual',
              route: '/cartera/analisis/vector-comportamiento',
              permiso: 'CARTERA_ANALISIS_VIEW'
            },

            {
              label: 'Por Corte',
              route: '/cartera/analisis/vector-comportamiento/corte',
              permiso: 'CARTERA_ANALISIS_VIEW'
            }

          ]
        },


        // =====================================================
        // MORA TEMPRANA
        // =====================================================
        {
          label: 'Mora Temprana',
          route: '/cartera/analisis/mora-temprana',
          permiso: 'CARTERA_ANALISIS_VIEW'
        },


        // =====================================================
        // RIESGO Y DETERIORO
        // =====================================================
        {
          label: 'Riesgo y Deterioro',
          route: '/cartera/analisis/riesgo-deterioro',
          permiso: 'CARTERA_ANALISIS_VIEW'
        },


        // =====================================================
        // CONCENTRACIÓN DE CARTERA
        // =====================================================
        {
          label: 'Concentración de Cartera',
          route: '/cartera/analisis/concentracion-cartera',
          permiso: 'CARTERA_ANALISIS_VIEW'
        },


        // =====================================================
        // GARANTÍAS Y COBERTURA
        // =====================================================
        {
          label: 'Garantías y Cobertura',
          route: '/cartera/analisis/garantias-cobertura',
          permiso: 'CARTERA_ANALISIS_VIEW'
        },


        // =====================================================
        // RECIPROCIDAD DE APORTES
        // =====================================================
        {
          label: 'Reciprocidad de Aportes',
          route: '/cartera/analisis/reciprocidad-aportes',
          permiso: 'CARTERA_ANALISIS_VIEW'
        },


        // =====================================================
        // MATRIZ DE RODAMIENTO
        // =====================================================
        {
          label: 'Matriz de Rodamiento',
          route: '/cartera/analisis/matriz-rodamiento',
          permiso: 'CARTERA_ANALISIS_VIEW'
        },


        // =====================================================
        // ANÁLISIS DE COSECHAS
        // =====================================================
        {
          label: 'Análisis de Cosechas',
          route: '/cartera/analisis/cosechas',
          permiso: 'CARTERA_ANALISIS_VIEW'
        },


        // =====================================================
        // CURACIÓN Y REINCIDENCIA
        // =====================================================
        {
          label: 'Curación y Reincidencia',
          route: '/cartera/analisis/curacion-reincidencia',
          permiso: 'CARTERA_ANALISIS_VIEW'
        },


        // =====================================================
        // CANCELACIÓN Y PREPAGO
        // =====================================================
        {
          label: 'Cancelación y Prepago',
          route: '/cartera/analisis/cancelacion-prepago',
          permiso: 'CARTERA_ANALISIS_VIEW'
        },


        // =====================================================
        // ROLL FORWARD
        // =====================================================
        {
          label: 'Roll Forward',
          route: '/cartera/analisis/roll-forward',
          permiso: 'CARTERA_ANALISIS_VIEW'
        }

      ]
    },


    // =========================================================
    // INFORMES
    // =========================================================
    {
      label: 'Informes',
      children: [

        // Consulta de créditos
        // Extractos
        // Tabla de amortización
        // Cartera por edades
        // Riesgos
        // Indicadores

      ]
    }

  ]
};

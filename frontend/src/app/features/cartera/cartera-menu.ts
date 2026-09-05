export const carteraMenu = {
  title: 'Cartera',
  icon: 'cartera',

  items: [

    // =============================
    // OPERACIÓN
    // =============================
    {
      label: 'Operación',
      children: [
        {
          label: 'Consulta de Créditos',
          route: '/cartera/consulta-creditos'
        }
      ]
    },

    // =============================
    // PROCESOS
    // =============================
    {
      label: 'Procesos',
      children: [

        // =============================
        // CIERRE MENSUAL
        // =============================
        // =============================
        // CIERRE MENSUAL
        // =============================
        {
          label: 'Cierre Mensual',
          children: [

            {
              label: 'Consolidación del Cierre',
              route: '/cartera/cierre-mensual'
            },

            {
              label: 'Cálculos',
              route: '/cartera/cierre-mensual/calculos'
            },

            {
              label: 'Anexo 1',
              route: '/cartera/cierre-mensual/anexo1'
            },

            {
              label: 'Anexo PE',
              route: '/cartera/cierre-mensual/anexo2'
            }

          ]
        },

        // =============================
        // EVALUACIÓN DE CARTERA
        // =============================
        {
          label: 'Evaluación de Cartera',
          children: [

            {
              label: 'Evaluaciones de Cartera',
              route: '/cartera/evaluacion/evaluaciones'
            },

            {
              label: 'Resultados Central de Riesgos',
              route: '/cartera/evaluacion/central-riesgos/resultados'
            }

          ]
        }

      ]
    },

    // =============================
    // ANÁLISIS DE CARTERA
    // =============================
    {
      label: 'Análisis de Cartera',
      children: [

        // =============================
        // VECTOR DE COMPORTAMIENTO
        // =============================
        {
          label: 'Vector de Comportamiento',
          children: [
            {
              label: 'Actual',
              route: '/cartera/analisis/vector-comportamiento'
            },
            {
              label: 'Por Corte',
              route: '/cartera/analisis/vector-comportamiento/corte'
            }
          ]
        },

        // =============================
        // MORA TEMPRANA
        // =============================
        {
          label: 'Mora Temprana',
          route: '/cartera/analisis/mora-temprana'
        },

        // =============================
        // RIESGO Y DETERIORO
        // =============================
        {
          label: 'Riesgo y Deterioro',
          route: '/cartera/analisis/riesgo-deterioro'
        },

        // =============================
        // CONCENTRACIÓN DE CARTERA
        // =============================
        {
          label: 'Concentración de Cartera',
          route: '/cartera/analisis/concentracion-cartera'
        },

        // =============================
        // GARANTÍAS Y COBERTURA
        // =============================
        {
          label: 'Garantías y Cobertura',
          route: '/cartera/analisis/garantias-cobertura'
        },

        // =============================
        // RECIPROCIDAD DE APORTES
        // =============================
        {
          label: 'Reciprocidad de Aportes',
          route: '/cartera/analisis/reciprocidad-aportes'
        },

        // =============================
        // MATRIZ DE RODAMIENTO
        // =============================
        {
          label: 'Matriz de Rodamiento',
          route: '/cartera/analisis/matriz-rodamiento'
        },

        // =============================
        // ANÁLISIS DE COSECHAS
        // =============================
        {
          label: 'Análisis de Cosechas',
          route: '/cartera/analisis/cosechas'
        },

        // =============================
        // CURACIÓN Y REINCIDENCIA
        // =============================
        {
          label: 'Curación y Reincidencia',
          route: '/cartera/analisis/curacion-reincidencia'
        },

        // =============================
        // CANCELACIÓN Y PREPAGO
        // =============================
        {
          label: 'Cancelación y Prepago',
          route: '/cartera/analisis/cancelacion-prepago'
        },

        // =============================
        // ROLL FORWARD
        // =============================
        {
          label: 'Roll Forward',
          route: '/cartera/analisis/roll-forward'
        }

      ]
    },

    // =============================
    // INFORMES
    // =============================
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

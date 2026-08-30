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
        {
          label: 'Cierre Mensual',
          route: '/cartera/cierre-mensual'
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
        // RIESGO Y DETERIORO
        // =============================
        {
          label: 'Riesgo y Deterioro',
          route: '/cartera/analisis/riesgo-deterioro'
        },

        // =============================
        // MORA TEMPRANA / CALIDAD DE ORIGINACIÓN
        // =============================
        {
          label: 'Mora Temprana',
          route: '/cartera/analisis/mora-temprana'
        },

        // =============================
        // CURACIÓN Y REINCIDENCIA
        // =============================
        {
          label: 'Curación y Reincidencia',
          route: '/cartera/analisis/curacion-reincidencia'
        },

        // =============================
        // CONCENTRACIÓN DE CARTERA
        // =============================
        {
          label: 'Concentración de Cartera',
          route: '/cartera/analisis/concentracion-cartera'
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

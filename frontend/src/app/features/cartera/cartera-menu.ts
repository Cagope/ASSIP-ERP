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

            // Próximos procesos:
            // Generación archivo para Centrales de Riesgo
            // Criterios de Evaluación
            // Reglas de Evaluación
          ]
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

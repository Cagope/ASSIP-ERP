
/**
 * Menú funcional de Contabilidad.
 * Las opciones se muestran según los permisos efectivos del usuario.
 * Agrupa comprobantes, informes y parametrización.
 */
export const contabilidadMenu = {
  title: 'Contabilidad',
  items: [
    // =========================================================
    // OPERACIÓN
    // =========================================================
    {
      label: 'Operación',
      children: [
        {
          label: 'Comprobantes',
          children: [
            {
              label: 'Crear comprobante',
              route: '/contabilidad/comprobantes',
              permiso: 'CONTABILIDAD_NEW'
            },
            {
              label: 'Consultar comprobantes',
              route: '/contabilidad/comprobantes/consultar',
              permiso: 'CONTABILIDAD_VIEW'
            },
            {
              label: 'Editar comprobante',
              route: '/contabilidad/comprobantes/editar',
              permiso: 'CONTABILIDAD_EDIT'
            },
            {
              label: 'Copiar comprobante',
              route: '/contabilidad/comprobantes/copiar',
              permiso: 'CONTABILIDAD_COPY'
            },
            {
              label: 'Importar comprobantes',
              route: '/contabilidad/comprobantes/importar',
              permiso: 'CONTABILIDAD_NEW'
            }
          ]
        }
      ]
    },

    // =========================================================
    // INFORMES
    // =========================================================
    {
      label: 'Informes',
      children: [
        {
          label: 'Auxiliares contables',
          children: [
            {
              label: 'Auxiliar por cuentas',
              route: '/contabilidad/informes/auxiliares/cuentas',
              permiso: 'CONTABILIDAD_AUXILIAR_CUENTAS_VIEW'
            }
          ]
        },
        {
          label: 'Informes especiales',
          children: [
            {
              label: 'Libro diario resumido',
              route: '/contabilidad/informes/especiales/libro-diario-resumido',
              permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW'
            },
            {
              label: 'Secuencial de comprobantes',
              route: '/contabilidad/informes/especiales/secuencial-comprobantes',
              permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW'
            },
            {
              label: 'Resumen diario detallado',
              route: '/contabilidad/informes/especiales/resumen-diario-detallado',
              permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW'
            },
            {
              label: 'Verificar la partida doble',
              route: '/contabilidad/informes/especiales/verificar-partida-doble',
              permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW'
            },
            {
              label: 'Comprobante resumen',
              route: '/contabilidad/informes/especiales/comprobante-resumen',
              permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW'
            }
          ]
        }
      ]
    },

    // =========================================================
    // PARAMETRIZACIÓN
    // =========================================================
    {
      label: 'Parametrización',
      children: [
        {
          label: 'Plan de Cuentas',
          route: '/contabilidad/plan-cuentas',
          permiso: 'CONTABILIDAD_VIEW'
        },
        {
          label: 'Tipos de Comprobantes',
          route: '/contabilidad/tipos-comprobantes',
          permiso: 'CONTABILIDAD_VIEW'
        }
      ]
    }
  ]
};

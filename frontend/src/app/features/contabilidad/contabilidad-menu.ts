
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
        },
        {
          label: 'Cierres',
          children: [
            {
              label: 'Cierre diario',
              route: '/contabilidad/cierres/diario',
              permiso: 'CONTABILIDAD_PERIODOS_VIEW'
            },
            {
              label: 'Cierre mensual contable',
              route: '/contabilidad/cierres/mensual',
              permiso: 'CONTABILIDAD_PERIODOS_VIEW'
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
            },
            {
              label: 'Exportación de auxiliares',
              route: '/contabilidad/informes/auxiliares/exportacion',
              permiso: 'CONTABILIDAD_AUXILIAR_CUENTAS_VIEW'
            }
          ]
        },

        // =====================================================
        // BALANCES
        // =====================================================
        {
          label: 'Balances',
          children: [
            {
              label: 'Balance detallado mensual',
              route: '/contabilidad/informes/balances',
              permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
            },
            {
              label: 'Balance de comprobación',
              route: '/contabilidad/informes/balances/comprobacion',
              permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
            },
            {
              label: 'Balance comparado mensual',
              route: '/contabilidad/informes/balances/comparado-mensual',
              permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
            },
            {
              label: 'Estado de cuentas',
              route: '/contabilidad/informes/balances/estado-cuentas',
              permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
            },
            {
              label: 'Balances por vigencias',
              route: '/contabilidad/informes/balances/vigencias',
              permiso: 'CONTABILIDAD_BALANCE_DETALLADO_MENSUAL_VIEW'
            }
          ]
        },

        // =====================================================
        // EXCEDENTES O PÉRDIDAS (PyG)
        // =====================================================
        {
          label: 'Excedentes o Pérdidas',
          children: [
            {
              label: 'Excedentes o pérdidas sin formato',
              route: '/contabilidad/informes/pyg/sin-formato',
              permiso: 'CONTABILIDAD_EXCEDENTES_PERDIDAS_VIEW'
            },
            {
              label: 'Excedentes o pérdidas con formato comparado',
              route: '/contabilidad/informes/pyg/comparado',
              permiso: 'CONTABILIDAD_EXCEDENTES_PERDIDAS_VIEW'
            },
            {
              label: 'Excedentes o pérdidas con formato individual',
              route: '/contabilidad/informes/pyg/individual',
              permiso: 'CONTABILIDAD_EXCEDENTES_PERDIDAS_VIEW'
            }
          ]
        },

        // =====================================================
        // INFORMES OFICIALES
        // =====================================================
        {
          label: 'Oficiales',
          children: [
            {
              label: 'Caja - Diario',
              route: '/contabilidad/informes/oficiales/caja-diario',
              permiso: 'CONTABILIDAD_INFORMES_OFICIALES_VIEW'
            },
            {
              label: 'Mayor y Balance',
              route: '/contabilidad/informes/oficiales/mayor-balance',
              permiso: 'CONTABILIDAD_INFORMES_OFICIALES_VIEW'
            }
          ]
        },

        // =====================================================
        // INFORMES ESPECIALES
        // =====================================================
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

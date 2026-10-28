
/**
 * Menú funcional de Contabilidad.
 *
 * Las opciones se muestran según los permisos
 * efectivos del usuario autenticado.
 *
 * Comprobantes se mantiene como una sola entrada
 * para agrupar sus operaciones actuales y futuras.
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
          route: '/contabilidad/comprobantes',
          permiso: 'CONTABILIDAD_NEW'
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

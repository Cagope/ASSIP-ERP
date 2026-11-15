// Únicamente rutas existentes; reglas de consulta separadas de informes.
export const sarlaftMenu = {
  title: 'Sarlaft',
  items: [
    { label: 'Regla 002 — Documento vs Edad', route: '/sarlaft/regla-002', permiso: 'SARLAFT_REGLAS_VIEW' },
    { label: 'Regla 003 — Forma 03 Prohibida', route: '/sarlaft/regla-003', permiso: 'SARLAFT_REGLAS_VIEW' },
    { label: 'Personas Desactualizadas', route: '/sarlaft/actualizacion', permiso: 'SARLAFT_INFORMES_VIEW' },
    { label: 'Datos Demográficos de Asociados', route: '/sarlaft/demograficos', permiso: 'SARLAFT_INFORMES_VIEW' },
    { label: 'Movimientos Inusuales', route: '/sarlaft/movimientos-inusuales', permiso: 'SARLAFT_INFORMES_VIEW' }
  ]
};

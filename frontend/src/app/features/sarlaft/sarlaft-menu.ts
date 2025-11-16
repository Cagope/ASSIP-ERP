// ========================================================
// 🛡️ Menú del esquema SARLAFT
// --------------------------------------------------------
// Opciones visibles en el panel lateral para el Oficial
// de Cumplimiento. 100% alineado con Hoja de Vida.
// ========================================================

export const sarlaftMenu = {
  title: '🛡️ SARLAFT',
  items: [
    // 🟦 Procesos
    { label: 'Reglas SARLAFT', route: '/sarlaft/reglas' },

    // 🆕 Reglas específicas
    { label: 'Regla 002 — Documento vs Edad', route: '/sarlaft/regla-002' },
    { label: 'Regla 003 — Forma 03 Prohibida', route: '/sarlaft/regla-003' },

    // 🟧 Informes
    { label: 'Personas Desactualizadas', route: '/sarlaft/informe-desactualizados' },
    { label: 'Datos Demográficos de Asociados', route: '/sarlaft/informe-demograficos' },
    { label: 'Alertas Entre Fechas', route: '/sarlaft/informe-alertas' },
    { label: 'Estado de Actualización', route: '/sarlaft/informe-estado' },
    { label: 'Movimientos Inusuales', route: '/sarlaft/informe-movimientos-inusuales' }
  ],
};

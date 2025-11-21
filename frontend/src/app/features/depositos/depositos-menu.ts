// ========================================================
// 🏦 Menú del esquema Depósitos
// --------------------------------------------------------
// Este archivo pertenece exclusivamente al equipo del esquema DEPÓSITOS.
// Aquí se definen las opciones del submenú visible en el panel lateral.
// ========================================================

export const depositosMenu = {
  title: '🏦 Depósitos',
  items: [
    // 💰 CUENTAS DE AHORRO
    { label: 'Cuentas de Ahorro', route: '/depositos/cuentas-ahorro' },

    // 🏛 FORMAS DE AHORRO
    { label: 'Formas de Ahorro', route: '/depositos/formas-ahorro' },

    // 📊 SALDOS A CORTE
    { label: 'Saldos a Corte', route: '/depositos/informes/saldos-corte' },

    // 🆕 CUENTAS NUEVAS O RETIRADAS
    { label: 'Cuentas Nuevas o Retiradas', route: '/depositos/informes/cuentas-nr' },

    // 🧩 INFORME — POR RANGOS
    { label: 'Informe por Rangos', route: '/depositos/informes/rangos' },

    // ⚠️ NUEVO INFORME — INCONSISTENCIAS DE SALDOS
    { label: 'Inconsistencias de Saldos', route: '/depositos/informes/inconsistencias' },

    // 📊 MOVIMIENTOS Y EXTRACTOS (pendiente)
    // { label: 'Movimientos y Extractos', route: '/depositos/movimientos' },

    // 🧮 LIQUIDACIÓN DE INTERESES (pendiente)
    // { label: 'Liquidación de Intereses', route: '/depositos/liquidacion' },
  ],
};

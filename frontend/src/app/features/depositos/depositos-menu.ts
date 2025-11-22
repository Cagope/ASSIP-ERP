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

    // 🏛 FORMAS DE Ahorro
    { label: 'Formas de Ahorro', route: '/depositos/formas-ahorro' },

    // ===============================
    // 📈 PROCESOS (NUEVA SECCIÓN)
    // ===============================
    { label: 'Revalorización de Aportes', route: '/depositos/procesos/revalorizacion' },

    // ⭐ NUEVO — INTERÉS DIARIO SM
    { label: 'Interés Diario SM', route: '/depositos/procesos/interes-diario-sm' },

    // ===============================
    // 🧾 INFORMES
    // ===============================

    // 📊 SALDOS A CORTE
    { label: 'Saldos a Corte', route: '/depositos/informes/saldos-corte' },

    // 🆕 CUENTAS NUEVAS O RETIRADAS
    { label: 'Cuentas Nuevas o Retiradas', route: '/depositos/informes/cuentas-nr' },

    // 🧩 INFORME — POR RANGOS
    { label: 'Informe por Rangos', route: '/depositos/informes/rangos' },

    // ⚠️ INFORME — INCONSISTENCIAS
    { label: 'Inconsistencias de Saldos', route: '/depositos/informes/inconsistencias' },
  ],
};

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

    // 📊 INFORMES
    { label: 'Saldos a Corte', route: '/depositos/informes/saldos-corte' },

    // 📊 MOVIMIENTOS Y EXTRACTOS (en desarrollo)
    // { label: 'Movimientos y Extractos', route: '/depositos/movimientos' },

    // 🧮 LIQUIDACIÓN DE INTERESES (pendiente)
    // { label: 'Liquidación de Intereses', route: '/depositos/liquidacion' },
  ],
};

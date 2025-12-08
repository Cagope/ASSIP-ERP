// ========================================================
// 🏦 Menú del esquema Depósitos
// --------------------------------------------------------
// Este archivo pertenece exclusivamente al equipo del esquema DEPÓSITOS.
// Aquí se definen las opciones del submenú visible en el panel lateral.
// ========================================================

export const depositosMenu = {
  title: '🏦 Depósitos',
  items: [
    //  CRUD DE AHORRO
    {
      label: 'Actualización de cuentas',
      route: '/depositos/cuentas-ahorro',
      permiso: 'DEPOSITOS_CUENTAS_VIEW'   // ⭐ SOLO ESTE CONTROLADO POR PERMISOS
    },

    // 💰 CUENTAS DE AHORRO
    { label: 'Consulta cuentas de Ahorro', route: '/depositos/informes/consulta-cuentas-ahorro' },

    // 🏛 FORMAS de Ahorro
    { label: 'Formas de Ahorro', route: '/depositos/formas-ahorro' },


    // ===============================
    // 📈 PROCESOS
    // ===============================

    { label: 'Revalorización de Aportes', route: '/depositos/procesos/revalorizacion' },
    { label: 'Interés Diario SM', route: '/depositos/procesos/interes-diario-sm' },
    { label: 'Interés Mensual SM', route: '/depositos/procesos/interes-mensual-sm' },
    { label: 'Interés Mensual TAC', route: '/depositos/procesos/interes-mensual-tac' },
    { label: 'Habilidad del Asociado', route: '/depositos/procesos/habilidad-asociado' },


    // ===============================
    // 🧾 INFORMES
    // ===============================

    { label: 'Saldos a Corte', route: '/depositos/informes/saldos-corte' },
    { label: 'Cuentas Nuevas o Retiradas', route: '/depositos/informes/cuentas-nr' },
    { label: 'Informe por Rangos', route: '/depositos/informes/rangos' },
    { label: 'Inconsistencias de Saldos', route: '/depositos/informes/inconsistencias' },
  ],
};

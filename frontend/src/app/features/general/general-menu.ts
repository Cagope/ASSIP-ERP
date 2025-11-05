// ========================================================
// 🏛️ Menú del esquema General
// Este archivo pertenece exclusivamente al equipo del esquema GENERAL.
// Aquí se definen las opciones del submenú visible en el panel lateral.
// ========================================================

export const generalMenu = {
  title: '🏛️ General',
  items: [
    { label: 'Agencias', route: '/general/agencias' },
    { label: 'Zonas', route: '/general/zonas' },
    { label: 'Subzonas', route: '/general/sub-zonas' },
    { label: 'Parámetros', route: '/general/parametros' }, // ✅ agregado
  ],
};

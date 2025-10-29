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
    { label: 'Subzonas', route: '/general/sub-zonas' }, // ✅ agregado
    // { label: 'Departamentos', route: '/general/departamentos' },
    // { label: 'Ciudades', route: '/general/ciudades' },
    // { label: 'Estados', route: '/general/estados' },
    // { label: 'Ubicaciones', route: '/general/ubicaciones' },
  ],
};

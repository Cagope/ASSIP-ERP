// ========================================================
// 🧾 Menú del esquema Hoja de Vida
// --------------------------------------------------------
// Este archivo pertenece exclusivamente al equipo del esquema HOJA DE VIDA.
// Aquí se definen las opciones del submenú visible en el panel lateral.
// ========================================================

export const hojaVidaMenu = {
  title: '🧾 Hoja de Vida',
  items: [
    { label: 'Datos Personales', route: '/hoja-vida/datos-personales' },
    { label: 'Ubicaciones', route: '/hoja-vida/ubicaciones' },
    { label: 'Información Laboral', route: '/hoja-vida/laborales' },
    { label: 'Datos Económicos', route: '/hoja-vida/financieros' },
    { label: 'Datos Familiares', route: '/hoja-vida/datos-familiares' },
    { label: 'Referencias Personales', route: '/hoja-vida/referencias-personales' },
    { label: 'SARLAFT', route: '/hoja-vida/sarlaft' }, // 🧩 nuevo módulo agregado
    { label: 'Permisos especiales', route: '/hoja-vida/permisos-especiales' }, // 🧩 nuevo módulo agregado

  ],
};

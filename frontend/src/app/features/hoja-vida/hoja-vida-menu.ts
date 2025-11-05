// ========================================================
// 🧾 Menú del esquema Hoja de Vida
// --------------------------------------------------------
// Este archivo pertenece exclusivamente al equipo del esquema HOJA DE VIDA.
// Aquí se definen las opciones del submenú visible en el panel lateral.
// ========================================================

export const hojaVidaMenu = {
  title: '🧾 Hoja de Vida',
  items: [
    // 🟩 FORMULARIO INTEGRAL
    { label: 'Formulario Integral', route: '/hoja-vida/formulario-integral' },

    // 🟦 CRUDS INDIVIDUALES
    { label: 'Datos Personales', route: '/hoja-vida/datos-personales' },
    { label: 'Ubicaciones', route: '/hoja-vida/ubicaciones' },
    { label: 'Información Laboral', route: '/hoja-vida/laborales' },
    { label: 'Datos Económicos', route: '/hoja-vida/financieros' },
    { label: 'Datos Familiares', route: '/hoja-vida/datos-familiares' },
    { label: 'Referencias Personales', route: '/hoja-vida/referencias-personales' },
    { label: 'SARLAFT', route: '/hoja-vida/sarlaft' },
    { label: 'Permisos Especiales', route: '/hoja-vida/permisos-especiales' },

    // 🧾 IMPRESIONES
    { label: '📄 Impresiones', route: '/hoja-vida/impresiones/afiliacion-list' },
  ],
};

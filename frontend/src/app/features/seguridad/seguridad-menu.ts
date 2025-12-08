// ========================================================
// 🔐 Menú del esquema Seguridad
// --------------------------------------------------------
// Este archivo pertenece exclusivamente al módulo SEGURIDAD.
// Aquí se definen las opciones del submenú visible en el panel lateral.
// ========================================================

export const seguridadMenu = {
  title: '🔐 Seguridad',
  items: [
    // 👤 CRUD Usuarios
    {
      label: 'Usuarios',
      route: '/seguridad/usuarios',
      permiso: 'USUARIOS_VIEW'   // ⭐ SE AGREGA SOLO ESTO
    },

    // Aquí se agregarán más submódulos cuando existan:
    // { label: 'Roles', route: '/seguridad/roles', permiso: 'ROLES_VIEW' },
    // { label: 'Permisos', route: '/seguridad/permisos', permiso: 'PERMISOS_VIEW' },
  ],
};

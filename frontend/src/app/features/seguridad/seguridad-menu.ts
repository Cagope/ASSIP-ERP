// ========================================================
// 🔐 Menú del esquema Seguridad
// --------------------------------------------------------
// Este archivo pertenece exclusivamente al módulo SEGURIDAD.
// Aquí se definen las opciones del submenú visible en el panel lateral.
// ========================================================

export const seguridadMenu = {
  title: 'Seguridad',
  items: [

    // =====================================================
    // USUARIOS
    // =====================================================

    {
      label: 'Usuarios',
      route: '/seguridad/usuarios',
      permiso: 'USUARIOS_VIEW'
    },

    // =====================================================
    // ROLES
    // =====================================================

    {
      label: 'Roles',
      route: '/seguridad/roles',
      permiso: 'ROLES_VIEW'
    },

    // =====================================================
    // PERMISOS
    // =====================================================

    {
      label: 'Permisos',
      route: '/seguridad/permisos',
      permiso: 'PERMISOS_VIEW'
    }

  ],
};

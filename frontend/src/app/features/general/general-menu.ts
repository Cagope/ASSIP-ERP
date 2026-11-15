// Permisos de consulta para mostrar opciones de General.
export const generalMenu = {
  title: 'General',
  items: [
    { label: 'Estructura Organizacional', children: [
      { label: 'Agencias', route: '/general/agencias', permiso: 'GENERAL_AGENCIAS_VIEW' },
      { label: 'Zonas', route: '/general/zonas', permiso: 'GENERAL_ZONAS_VIEW' },
      { label: 'Subzonas', route: '/general/sub-zonas', permiso: 'GENERAL_SUBZONAS_VIEW' }
    ] },
    { label: 'Parámetros', children: [
      { label: 'Parámetros generales', route: '/general/parametros', permiso: 'GENERAL_PARAMETROS_VIEW' }
    ] },
    { label: 'Catálogos', children: [
      { label: 'Formas de ahorro', route: '/depositos/formas-ahorro', permiso: 'DEPOSITOS_FORMAS_AHORRO_VIEW' }
    ] }
  ]
};

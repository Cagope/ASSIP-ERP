export const generalMenu = {
  title: 'General',
  items: [

    {
      label: 'Estructura Organizacional',
      children: [
        { label: 'Agencias', route: '/general/agencias' },
        { label: 'Zonas', route: '/general/zonas' },
        { label: 'Subzonas', route: '/general/sub-zonas' }
      ]
    },

    {
      label: 'Parámetros',
      children: [
        { label: 'Parámetros generales', route: '/general/parametros' }
      ]
    },

    {
      label: 'Catálogos',
      children: [
        {
          label: 'Formas de ahorro',
          route: '/depositos/formas-ahorro'
        }
      ]
    }

  ]
};

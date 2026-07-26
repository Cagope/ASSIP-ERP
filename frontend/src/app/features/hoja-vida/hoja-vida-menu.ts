// ========================================================
// 🧾 Menú del esquema Hoja de Vida
// --------------------------------------------------------
// Este archivo pertenece exclusivamente al equipo del esquema HOJA DE VIDA.
// Aquí se definen las opciones del submenú visible en el panel lateral.
// ========================================================

export const hojaVidaMenu = {
  title: 'Hoja de Vida',
  items: [

    // 🟩 FORMULARIO INTEGRAL
    {
      label: 'Formulario Integral',
      route: '/hoja-vida/formulario-integral'
    },

    // 🟦 CRUDS INDIVIDUALES
    {
      label: 'Datos Personales',
      route: '/hoja-vida/datos-personales'
    },
    {
      label: 'Dirección y Contacto',
      route: '/hoja-vida/ubicaciones'
    },
    {
      label: 'Información Laboral',
      route: '/hoja-vida/laborales'
    },
    {
      label: 'Datos Económicos',
      route: '/hoja-vida/financieros'
    },
    {
      label: 'Referencias Familiares',
      route: '/hoja-vida/datos-familiares'
    },
    {
      label: 'Referencias Personales',
      route: '/hoja-vida/referencias-personales'
    },
    {
      label: 'Declaración LA / FT',
      route: '/hoja-vida/sarlaft'
    },
    {
      label: 'Residencia Fiscal (FATCA / CRS)',
      route: '/hoja-vida/residencia-fiscal'
    },
    {
      label: 'Condiciones de Protección',
      route: '/hoja-vida/condiciones-proteccion'
    },
    {
      label: 'Autorizaciones de Contacto',
      route: '/hoja-vida/permisos-especiales'
    },

    // 🏠 BIENES
    {
      label: 'Bienes Patrimoniales',
      children: [
        {
          label: 'Inmuebles',
          route: '/hoja-vida/bienes-inmuebles'
        },
        {
          label: 'Vehículos',
          route: '/hoja-vida/bienes-vehiculos'
        },
        {
          label: 'Maquinaria',
          route: '/hoja-vida/bienes-maquinaria'
        },
        {
          label: 'Inversiones',
          route: '/hoja-vida/bienes-inversiones'
        }
      ]
    },

    {
      label: 'Apertura de Cuentas',
      route: '/hoja-vida/apertura-cuentas'
    },

    // 🧾 IMPRESIONES
    {
      label: 'Impresión de Formatos',
      route: '/hoja-vida/impresiones/afiliacion-list'
    }

  ]
};

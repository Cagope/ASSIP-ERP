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
    // Pendiente definir permiso específico según su alcance funcional.
    {
      label: 'Formulario Integral',
      route: '/hoja-vida/formulario-integral',
      permiso: 'HOJAVIDA_FORMULARIO_INTEGRAL_EDIT'
    },

    // 🟦 CRUDS INDIVIDUALES
    {
      label: 'Datos Personales',
      route: '/hoja-vida/datos-personales',
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    },
    {
      label: 'Dirección y Contacto',
      route: '/hoja-vida/ubicaciones',
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    },
    {
      label: 'Información Laboral',
      route: '/hoja-vida/laborales',
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    },
    {
      label: 'Datos Económicos',
      route: '/hoja-vida/financieros',
      permiso: 'HOJAVIDA_FINANCIERO_VIEW'
    },
    {
      label: 'Referencias Familiares',
      route: '/hoja-vida/datos-familiares',
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    },
    {
      label: 'Referencias Personales',
      route: '/hoja-vida/referencias-personales',
      permiso: 'HOJAVIDA_PERSONA_VIEW'
    },
    {
      label: 'Declaración LA / FT',
      route: '/hoja-vida/sarlaft',
      permiso: 'HOJAVIDA_SARLAFT_VIEW'
    },
    {
      label: 'Residencia Fiscal (FATCA / CRS)',
      route: '/hoja-vida/residencia-fiscal',
      permiso: 'HOJAVIDA_FISCAL_VIEW'
    },
    {
      label: 'Condiciones de Protección',
      route: '/hoja-vida/condiciones-proteccion',
      permiso: 'HOJAVIDA_PROTECCION_VIEW'
    },
    {
      label: 'Autorizaciones de Contacto',
      route: '/hoja-vida/permisos-especiales',
      permiso: 'HOJAVIDA_PROTECCION_VIEW'
    },

    // 🏠 BIENES
    {
      label: 'Bienes Patrimoniales',
      children: [
        {
          label: 'Inmuebles',
          route: '/hoja-vida/bienes-inmuebles',
          permiso: 'HOJAVIDA_BIENES_VIEW'
        },
        {
          label: 'Vehículos',
          route: '/hoja-vida/bienes-vehiculos',
          permiso: 'HOJAVIDA_BIENES_VIEW'
        },
        {
          label: 'Maquinaria',
          route: '/hoja-vida/bienes-maquinaria',
          permiso: 'HOJAVIDA_BIENES_VIEW'
        },
        {
          label: 'Inversiones',
          route: '/hoja-vida/bienes-inversiones',
          permiso: 'HOJAVIDA_BIENES_VIEW'
        }
      ]
    },

    // Pendiente definir permiso según el proceso que ejecuta.
    {
      label: 'Apertura de Cuentas',
      route: '/hoja-vida/apertura-cuentas',
      permiso: 'HOJAVIDA_APERTURA_CUENTAS'
    },

    // 🧾 IMPRESIONES
    {
      label: 'Impresión de Formatos',
      route: '/hoja-vida/impresiones/afiliacion-list',
      permiso: 'HOJAVIDA_FORMATOS_GENERATE'
    }

  ]
};

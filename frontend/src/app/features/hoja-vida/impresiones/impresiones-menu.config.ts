export interface FormatoImpresion {
  id: string;
  label: string;
  icon: string;
  ruta: string;
}

export const FORMATOS_IMPRESION: FormatoImpresion[] = [
  {
    id: 'afiliacion',
    label: 'Formulario de Afiliación',
    icon: '📝',
    ruta: '/hoja-vida/impresiones/afiliacion-formulario'
  },
  {
    id: 'tratamiento',
    label: 'Tratamiento Datos Personales',
    icon: '🔒',
    ruta: '/hoja-vida/impresiones/tratamiento-datos'
  },
  {
    id: 'origen-fondos',
    label: 'Declaración Origen de Fondos',
    icon: '💰',
    ruta: '/hoja-vida/impresiones/origen-fondos'
  },
  {
    id: 'carta-gmf',
    label: 'Carta Exoneración GMF',
    icon: '📄',
    ruta: '/hoja-vida/impresiones/carta-gmf'
  },
  {
    id: 'actualizacion-datos', // ✅ Nuevo formato agregado
    label: 'Actualización Datos Persona Naturales',
    icon: '📋',
    ruta: '/hoja-vida/impresiones/actualizacion-datos'
  }
];

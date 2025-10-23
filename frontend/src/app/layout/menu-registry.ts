// ========================================================
// 📚 Registro central de menús por esquema (solo lectura)
// Cada esquema mantiene su propio archivo de menú.
// ========================================================

import { hojaVidaMenu } from '../features/hoja-vida/hoja-vida-menu';

// En el futuro aquí se agregan más:
// import { contabilidadMenu } from '../features/contabilidad/contabilidad-menu';
// import { activosFijosMenu } from '../features/activos-fijos/activos-fijos-menu';

export const MENU_REGISTRY = [
  hojaVidaMenu,
  // contabilidadMenu,
  // activosFijosMenu,
];

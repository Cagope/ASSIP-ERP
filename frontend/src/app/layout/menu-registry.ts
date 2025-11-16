// ========================================================
// 📚 Registro central de menús por esquema (solo lectura)
// Cada esquema mantiene su propio archivo de menú.
// ========================================================

import { generalMenu } from '../features/general/general-menu';
import { hojaVidaMenu } from '../features/hoja-vida/hoja-vida-menu';
import { depositosMenu } from '../features/depositos/depositos-menu'; // 🟦 nuevo
import { sarlaftMenu } from '../features/sarlaft/sarlaft-menu';


// En el futuro aquí se agregan más:
// import { contabilidadMenu } from '../features/contabilidad/contabilidad-menu';
// import { activosFijosMenu } from '../features/activos-fijos/activos-fijos-menu';

export const MENU_REGISTRY = [
  generalMenu,
  hojaVidaMenu,
  depositosMenu, // ✅ ahora visible en el panel lateral
  sarlaftMenu,
  // contabilidadMenu,
  // activosFijosMenu,
];

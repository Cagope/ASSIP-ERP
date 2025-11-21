// ========================================================
// 📚 Registro central de menús por esquema (solo lectura)
// Cada esquema mantiene su propio archivo de menú.
// ========================================================

import { generalMenu } from '../features/general/general-menu';
import { hojaVidaMenu } from '../features/hoja-vida/hoja-vida-menu';
import { depositosMenu } from '../features/depositos/depositos-menu';
import { sarlaftMenu } from '../features/sarlaft/sarlaft-menu';
import { contabilidadMenu } from '../features/contabilidad/contabilidad-menu';

import { sesMenu } from '../features/ses/ses-menu';   // ✅ NUEVO MENÚ SES

// import { activosFijosMenu } from '../features/activos-fijos/activos-fijos-menu';

export const MENU_REGISTRY = [
  generalMenu,
  hojaVidaMenu,
  depositosMenu,
  sarlaftMenu,
  contabilidadMenu,
  sesMenu,           // ✅ AGREGADO AL MENÚ PRINCIPAL
  // activosFijosMenu,
];

import { generalMenu } from '../features/general/general-menu';
import { hojaVidaMenu } from '../features/hoja-vida/hoja-vida-menu';
import { depositosMenu } from '../features/depositos/depositos-menu';
import { sarlaftMenu } from '../features/sarlaft/sarlaft-menu';
import { contabilidadMenu } from '../features/contabilidad/contabilidad-menu';

import { sesMenu } from '../features/ses/ses-menu';
import { seguridadMenu } from '../features/seguridad/seguridad-menu';
import { activosFijosMenu } from '../features/activos-fijos/activos-fijos-menu'; // ✅
import { nominaMenu } from '../features/nomina/nomina-menu'; // ✅ NUEVO

export const MENU_REGISTRY = [
  generalMenu,
  hojaVidaMenu,
  seguridadMenu,
  depositosMenu,
  sarlaftMenu,
  contabilidadMenu,
  activosFijosMenu, // ✅
  nominaMenu,      // ✅ NUEVO
  sesMenu,
];

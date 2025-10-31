import { Routes } from '@angular/router';

import { DATOS_PERSONALES_ROUTES } from './datos-personales/datos-personales.routes';
import { UBICACIONES_ROUTES } from './ubicaciones/ubicaciones.routes';
import { LABORALES_ROUTES } from './laborales/laborales.routes';
import { FINANCIEROS_ROUTES } from './financieros/financieros.routes';
import { DATOS_FAMILIARES_ROUTES } from './datos-familiares/datos-familiares.routes';
import { REFERENCIAS_PERSONALES_ROUTES } from './referencias-personales/referencias-personales.routes';
import { SARLAFT_ROUTES } from './sarlaft/sarlaft.routes';
import { PERMISOS_ESPECIALES_ROUTES } from './permisos-especiales/permisos-especiales.routes';

/**
 * 🧩 Rutas principales — Módulo Hoja de Vida
 * ------------------------------------------------------------
 * Estructura base de navegación interna del módulo:
 *  - Datos Personales
 *  - Ubicaciones
 *  - Laborales
 *  - Financieros
 *  - Datos Familiares
 *  - Referencias Personales
 *  - SARLAFT
 *  - Permisos Especiales
 */
export const HOJA_VIDA_ROUTES: Routes = [
  { path: 'datos-personales', children: DATOS_PERSONALES_ROUTES },
  { path: 'ubicaciones', children: UBICACIONES_ROUTES },
  { path: 'laborales', children: LABORALES_ROUTES },
  { path: 'financieros', children: FINANCIEROS_ROUTES },
  { path: 'datos-familiares', children: DATOS_FAMILIARES_ROUTES },
  { path: 'referencias-personales', children: REFERENCIAS_PERSONALES_ROUTES },
  { path: 'sarlaft', children: SARLAFT_ROUTES },
  { path: 'permisos-especiales', children: PERMISOS_ESPECIALES_ROUTES },
];

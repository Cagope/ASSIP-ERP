import { Routes } from '@angular/router';

import { DATOS_PERSONALES_ROUTES } from './datos-personales/datos-personales.routes';
import { UBICACIONES_ROUTES } from './ubicaciones/ubicaciones.routes';
import { LABORALES_ROUTES } from './laborales/laborales.routes';
import { FINANCIEROS_ROUTES } from './financieros/financieros.routes';
import { DATOS_FAMILIARES_ROUTES } from './datos-familiares/datos-familiares.routes';
import { REFERENCIAS_PERSONALES_ROUTES } from './referencias-personales/referencias-personales.routes';
import { SARLAFT_ROUTES } from './sarlaft/sarlaft.routes';
import { PERMISOS_ESPECIALES_ROUTES } from './permisos-especiales/permisos-especiales.routes';
import { APERTURA_CUENTAS_ROUTES } from './apertura-cuentas/apertura-cuentas.routes';

/** ✅ Flujo integral (wizard) */
import { FORMULARIO_INTEGRAL_ROUTES } from './formulario-integral/formulario-integral.routes';

/** 🧾 Nuevo módulo de impresiones */
import { AFILIACION_ROUTES } from './impresiones/afiliacion.routes';
import { BIENES_INMUEBLES_ROUTES } from './bienes-inmuebles/bienes-inmuebles.routes';
import { BIENES_VEHICULOS_ROUTES } from './bienes-vehiculos/bienes-vehiculos.routes';
import { BIENES_MAQUINARIA_ROUTES } from './bienes-maquinaria/bienes-maquinaria.routes';
import { BIENES_INVERSIONES_ROUTES } from './bienes-inversiones/bienes-inversiones.routes';
/**
 * 🧩 Rutas principales — Módulo Hoja de Vida
 * ------------------------------------------------------------
 * Estructura base de navegación interna del módulo:
 *  - Formulario Integral (flujo completo)
 *  - CRUDs tradicionales
 *  - Módulo de impresiones (nuevos documentos)
 */
export const HOJA_VIDA_ROUTES: Routes = [
  // 🟩 Flujo integral (wizard)
  { path: 'formulario-integral', children: FORMULARIO_INTEGRAL_ROUTES },

  // 🟦 CRUDs tradicionales
  { path: 'datos-personales', children: DATOS_PERSONALES_ROUTES },
  { path: 'ubicaciones', children: UBICACIONES_ROUTES },
  { path: 'laborales', children: LABORALES_ROUTES },
  { path: 'financieros', children: FINANCIEROS_ROUTES },
  { path: 'datos-familiares', children: DATOS_FAMILIARES_ROUTES },
  { path: 'referencias-personales', children: REFERENCIAS_PERSONALES_ROUTES },
  { path: 'sarlaft', children: SARLAFT_ROUTES },
  { path: 'permisos-especiales', children: PERMISOS_ESPECIALES_ROUTES },
  { path: 'apertura-cuentas', children: APERTURA_CUENTAS_ROUTES },
  { path: 'bienes-inmuebles', children: BIENES_INMUEBLES_ROUTES },
  { path: 'bienes-vehiculos', children: BIENES_VEHICULOS_ROUTES },
  { path: 'bienes-maquinaria', children: BIENES_MAQUINARIA_ROUTES },
  { path: 'bienes-inversiones', children: BIENES_INVERSIONES_ROUTES },

  // 🧾 Nuevo módulo de impresiones
  { path: 'impresiones', children: AFILIACION_ROUTES },
];

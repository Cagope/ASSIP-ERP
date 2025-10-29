import { Routes } from '@angular/router';

import { DATOS_PERSONALES_ROUTES } from './datos-personales/datos-personales.routes';
import { UBICACIONES_ROUTES } from './ubicaciones/ubicaciones.routes';
import { LABORALES_ROUTES } from './laborales/laborales.routes'; // 🆕 Importación del módulo Laborales
// import { ECONOMICOS_ROUTES } from './economicos/economicos.routes';
// import { FAMILIARES_ROUTES } from './familiares/familiares.routes';
// import { REFERENCIAS_ROUTES } from './referencias/referencias.routes';

export const HOJA_VIDA_ROUTES: Routes = [
  {
    path: 'datos-personales',
    children: DATOS_PERSONALES_ROUTES,
  },
  {
    path: 'ubicaciones',
    children: UBICACIONES_ROUTES,
  },
  {
    path: 'laborales',
    children: LABORALES_ROUTES, // 🆕 Nueva sección: Información Laboral
  },
  // {
  //   path: 'economicos',
  //   children: ECONOMICOS_ROUTES,
  // },
  // {
  //   path: 'familiares',
  //   children: FAMILIARES_ROUTES,
  // },
  // {
  //   path: 'referencias',
  //   children: REFERENCIAS_ROUTES,
  // },
];

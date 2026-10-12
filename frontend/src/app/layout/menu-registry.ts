import { hojaVidaMenu } from '../features/hoja-vida/hoja-vida-menu';
import { depositosMenu } from '../features/depositos/depositos-menu';
import { carteraMenu } from '../features/cartera/cartera-menu';
import { cdatMenu } from '../features/cdat/cdat-menu';
import { consultasMenu } from '../features/consultas/consultas-menu';
import { cajasMenu } from '../features/cajas/cajas.menu';
import { tesoreriaMenu } from '../features/tesoreria/tesoreria-menu';

import { sarlaftMenu } from '../features/sarlaft/sarlaft-menu';
import { contabilidadMenu } from '../features/contabilidad/contabilidad-menu';

import { sesMenu } from '../features/ses/ses-menu';
import { seguridadMenu } from '../features/seguridad/seguridad-menu';
import { activosFijosMenu } from '../features/activos-fijos/activos-fijos-menu';
import { nominaMenu } from '../features/nomina/nomina-menu';
import { generalMenu } from '../features/general/general-menu';

export const MENU_REGISTRY = [

  { ...hojaVidaMenu, icon: 'hojaVida' },

  { ...depositosMenu, icon: 'depositos' },

  { ...carteraMenu, icon: 'cartera' },

  { ...cdatMenu, icon: 'cdat' },

  { ...consultasMenu, icon: 'consultas' },

  { ...cajasMenu, icon: 'cajas' },

  { ...tesoreriaMenu, icon: 'tesoreria' },

  { ...activosFijosMenu, icon: 'activos' },

  { ...nominaMenu, icon: 'nomina' },

  { ...contabilidadMenu, icon: 'contabilidad' },

  { ...sarlaftMenu, icon: 'sarlaf' },

  { ...sesMenu, icon: 'superintendencia' },

  { ...generalMenu, icon: 'general' },

  { ...seguridadMenu, icon: 'seguridad' }

];

import { generalMenu } from '../features/general/general-menu';
import { hojaVidaMenu } from '../features/hoja-vida/hoja-vida-menu';
import { depositosMenu } from '../features/depositos/depositos-menu';
import { cdatMenu } from '../features/cdat/cdat-menu';
import { sarlaftMenu } from '../features/sarlaft/sarlaft-menu';
import { contabilidadMenu } from '../features/contabilidad/contabilidad-menu';

import { sesMenu } from '../features/ses/ses-menu';
import { seguridadMenu } from '../features/seguridad/seguridad-menu';
import { activosFijosMenu } from '../features/activos-fijos/activos-fijos-menu';
import { nominaMenu } from '../features/nomina/nomina-menu';

import { GERENCIA_MENU } from '../features/gerencia/gerencia-menu';

export const MENU_REGISTRY = [

  { ...hojaVidaMenu, icon: 'hojaVida' },

  { ...depositosMenu, icon: 'depositos' },

  { ...cdatMenu, icon: 'cdat' },

  { ...contabilidadMenu, icon: 'contabilidad' },

  { ...activosFijosMenu, icon: 'activos' },

  { ...nominaMenu, icon: 'nomina' },

  {
    ...GERENCIA_MENU,
    icon: 'gerencia'
  },

  { ...sarlaftMenu, icon: 'sarlaf' },

  { ...sesMenu, icon: 'superintendencia' },

  { ...generalMenu, icon: 'general' },

  { ...seguridadMenu, icon: 'seguridad' }

];

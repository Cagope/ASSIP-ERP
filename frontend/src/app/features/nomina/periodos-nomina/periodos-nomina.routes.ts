import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { PeriodosNominaListComponent } from './periodos-nomina-list.component';
import { PeriodosGeneradorComponent } from './periodos-generador.component';
import { NovedadesNominaMasivoComponent } from '../novedades-nomina/novedades-nomina-masivo.component';

export const PERIODOS_NOMINA_ROUTES: Routes = [

  {
    path: '',
    component: PeriodosNominaListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },

  {
    path: 'generador',
    component: PeriodosGeneradorComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_PARAMETROS_EDIT' }
  },

  {
    path: 'masivo',
    component: NovedadesNominaMasivoComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_NOVEDADES_EDIT' }
  }

];

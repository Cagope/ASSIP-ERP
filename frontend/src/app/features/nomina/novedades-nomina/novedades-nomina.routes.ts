import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { NovedadesNominaListComponent } from './novedades-nomina-list.component';
import { NovedadesNominaUpsertComponent } from './novedades-nomina-upsert.component';
import { NovedadesNominaMasivoComponent } from './novedades-nomina-masivo.component';

export const NOVEDADES_NOMINA_ROUTES: Routes = [
  {
    path: '',
    component: NovedadesNominaListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_VIEW' }
  },
  {
    path: 'nuevo',
    component: NovedadesNominaUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_NOVEDADES_EDIT' }
  },
  {
    path: 'masivo',
    component: NovedadesNominaMasivoComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_NOVEDADES_EDIT' }
  },
  {
    path: ':id',
    component: NovedadesNominaUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_NOVEDADES_EDIT' }
  }
];

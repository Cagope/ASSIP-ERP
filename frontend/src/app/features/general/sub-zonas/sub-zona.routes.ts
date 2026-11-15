import { Routes } from '@angular/router';
import { SubZonaListComponent } from './sub-zona-list.component';
import { SubZonaUpsertComponent } from './sub-zona-upsert.component';

import { permisoGuard } from '../../../core/auth/permiso.guard';

export const SUB_ZONAS_ROUTES: Routes = [
  { path: '', component: SubZonaListComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_SUBZONAS_VIEW' } },
  { path: 'nuevo', component: SubZonaUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_SUBZONAS_EDIT' } },
  { path: ':id/editar', component: SubZonaUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_SUBZONAS_EDIT' } },
];

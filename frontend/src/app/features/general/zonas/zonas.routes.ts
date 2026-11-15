import { Routes } from '@angular/router';
import { ZonaListComponent } from './zona-list.component';
import { ZonaUpsertComponent } from './zona-upsert.component';

import { permisoGuard } from '../../../core/auth/permiso.guard';

export const ZONAS_ROUTES: Routes = [
  { path: '', component: ZonaListComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_ZONAS_VIEW' } },
  { path: 'nuevo', component: ZonaUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_ZONAS_EDIT' } },
  { path: ':id/editar', component: ZonaUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_ZONAS_EDIT' } },
];

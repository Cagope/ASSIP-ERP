import { Routes } from '@angular/router';
import { BloquesListComponent } from './bloques-list.component';
import { BloquesUpsertComponent } from './bloques-upsert.component';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const BLOQUES_ROUTES: Routes = [

  // =========================================================
  // CONSULTA
  // =========================================================
  {
    path: '',
    component: BloquesListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_VIEW' }
  },

  // =========================================================
  // NUEVO
  // =========================================================
  {
    path: 'nuevo',
    component: BloquesUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_EDIT' }
  },

  // =========================================================
  // EDICIÓN
  // =========================================================
  {
    path: ':id/editar',
    component: BloquesUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_EDIT' }
  }

];

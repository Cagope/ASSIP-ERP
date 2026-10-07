import { Routes } from '@angular/router';
import { ActivosFijosListComponent } from './activos-fijos-list.component';
import { ActivosFijosUpsertComponent } from './activos-fijos-upsert.component';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const ACTIVOS_ROUTES: Routes = [

  // =========================================================
  // CONSULTA DE ACTIVOS
  // =========================================================
  {
    path: '',
    component: ActivosFijosListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_VIEW' }
  },

  // =========================================================
  // NUEVO ACTIVO
  // =========================================================
  {
    path: 'nuevo',
    component: ActivosFijosUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_EDIT' }
  },

  // =========================================================
  // EDICIÓN DE ACTIVO
  // =========================================================
  {
    path: ':id/editar',
    component: ActivosFijosUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_EDIT' }
  }

];

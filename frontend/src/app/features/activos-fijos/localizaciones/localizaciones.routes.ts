import { Routes } from '@angular/router';
import { LocalizacionesListComponent } from './localizaciones-list.component';
import { LocalizacionesUpsertComponent } from './localizaciones-upsert.component';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const LOCALIZACIONES_ROUTES: Routes = [
  {
    path: '',
    component: LocalizacionesListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_VIEW' }
  },
  {
    path: 'nuevo',
    component: LocalizacionesUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_EDIT' }
  },
  {
    path: ':id/editar',
    component: LocalizacionesUpsertComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_EDIT' }
  }
];

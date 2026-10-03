import { Routes } from '@angular/router';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const CDATS_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./cdats-list.component')
        .then(m => m.CdatsListComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_CUENTAS_VIEW'
    }
  },

  {
    path: 'nuevo',
    loadComponent: () =>
      import('./cdats-upsert.component')
        .then(m => m.CdatsUpsertComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_APERTURA_CREATE'
    }
  },

  {
    path: 'editar/:id',
    loadComponent: () =>
      import('./cdats-upsert.component')
        .then(m => m.CdatsUpsertComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_CUENTAS_VIEW'
    }
  }

];

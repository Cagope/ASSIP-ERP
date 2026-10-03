import { Routes } from '@angular/router';
import { FormasAhorroListComponent } from './formas-ahorro-list.component';
import { FormasAhorroUpsertComponent } from './formas-ahorro-upsert.component';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const FORMAS_AHORRO_ROUTES: Routes = [
  {
    path: '',
    component: FormasAhorroListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_FORMAS_AHORRO_VIEW'
    }
  },
  {
    path: 'nuevo',
    component: FormasAhorroUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_FORMAS_AHORRO_EDIT'
    }
  },
  {
    path: ':id/editar',
    component: FormasAhorroUpsertComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_FORMAS_AHORRO_EDIT'
    }
  }
];

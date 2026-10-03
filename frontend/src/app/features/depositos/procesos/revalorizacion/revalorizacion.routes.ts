import { Routes } from '@angular/router';
import { RevalorizacionListComponent } from './revalorizacion-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const revalorizacionRoutes: Routes = [
  {
    path: '',
    component: RevalorizacionListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_REVALORIZACION_EXECUTE'
    }
  }
];

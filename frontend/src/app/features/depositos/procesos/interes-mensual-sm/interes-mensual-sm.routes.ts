import { Routes } from '@angular/router';
import { InteresMensualSmListComponent } from './interes-mensual-sm-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const interesMensualSmRoutes: Routes = [
  {
    path: '',
    component: InteresMensualSmListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INTERESES_EXECUTE'
    }
  }
];

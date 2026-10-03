import { Routes } from '@angular/router';
import { InteresDiarioSmListComponent } from './interes-diario-sm-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const interesDiarioSmRoutes: Routes = [
  {
    path: '',
    component: InteresDiarioSmListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INTERESES_EXECUTE'
    }
  }
];

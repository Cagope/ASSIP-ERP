import { Routes } from '@angular/router';
import { InteresMensualTacListComponent } from './interes-mensual-tac-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const interesMensualTacRoutes: Routes = [
  {
    path: '',
    component: InteresMensualTacListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INTERESES_EXECUTE'
    }
  }
];

import { Routes } from '@angular/router';
import { InconsistenciasListComponent } from './inconsistencias-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const INCONSISTENCIAS_ROUTES: Routes = [
  {
    path: '',
    component: InconsistenciasListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

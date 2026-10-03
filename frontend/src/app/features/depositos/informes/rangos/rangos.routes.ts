import { Routes } from '@angular/router';
import { RangosListComponent } from './rangos-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const RANGOS_ROUTES: Routes = [
  {
    path: '',
    title: 'Informe por Rangos',
    component: RangosListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

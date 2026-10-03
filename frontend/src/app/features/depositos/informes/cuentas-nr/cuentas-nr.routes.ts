import { Routes } from '@angular/router';
import { CuentasNRListComponent } from './cuentas-nr-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const cuentasNRRoutes: Routes = [
  {
    path: '',
    component: CuentasNRListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];

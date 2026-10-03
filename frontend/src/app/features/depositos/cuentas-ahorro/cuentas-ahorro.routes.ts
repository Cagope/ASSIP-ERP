import { Routes } from '@angular/router';
import { CuentasAhorroComponent } from './cuentas-ahorro.component';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const CUENTAS_AHORRO_ROUTES: Routes = [
  {
    path: '',
    component: CuentasAhorroComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_CUENTAS_VIEW'
    }
  }
];

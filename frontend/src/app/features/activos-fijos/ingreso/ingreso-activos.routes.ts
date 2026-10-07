import { Routes } from '@angular/router';
import { IngresoActivosComponent } from './ingreso-activos.component';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const INGRESO_ACTIVOS_ROUTES: Routes = [
  {
    path: '',
    component: IngresoActivosComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_INGRESO' }
  }
];

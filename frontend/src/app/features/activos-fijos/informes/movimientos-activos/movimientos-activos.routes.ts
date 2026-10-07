import { Routes } from '@angular/router';
import { MovimientosActivosListComponent } from './movimientos-activos-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const movimientosActivosRoutes: Routes = [
  {
    path: '',
    component: MovimientosActivosListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_INFORMES_VIEW' }
  }
];

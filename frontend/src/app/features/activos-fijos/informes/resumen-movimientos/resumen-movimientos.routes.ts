import { Routes } from '@angular/router';
import { ResumenMovimientosListComponent } from './resumen-movimientos-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const resumenMovimientosRoutes: Routes = [
  {
    path: '',
    component: ResumenMovimientosListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_INFORMES_VIEW' }
  }
];

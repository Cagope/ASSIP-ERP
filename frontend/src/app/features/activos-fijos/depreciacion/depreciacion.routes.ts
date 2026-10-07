import { Routes } from '@angular/router';
import { DepreciacionComponent } from './depreciacion.component';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const depreciacionRoutes: Routes = [
  {
    path: '',
    component: DepreciacionComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_DEPRECIACION' }
  }
];

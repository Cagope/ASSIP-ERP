import { Routes } from '@angular/router';
import { KardexActivoListComponent } from './kardex-activo-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const kardexActivoRoutes: Routes = [
  {
    path: '',
    component: KardexActivoListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_INFORMES_VIEW' }
  }
];

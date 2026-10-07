import { Routes } from '@angular/router';
import { DepreciacionInformeListComponent } from './depreciacion-informe-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const depreciacionInformeRoutes: Routes = [
  {
    path: '',
    component: DepreciacionInformeListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_INFORMES_VIEW' }
  }
];

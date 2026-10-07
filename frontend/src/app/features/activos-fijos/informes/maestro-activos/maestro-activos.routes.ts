import { Routes } from '@angular/router';
import { MaestroActivosListComponent } from './maestro-activos-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const maestroActivosRoutes: Routes = [
  {
    path: '',
    component: MaestroActivosListComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'ACTIVOS_FIJOS_INFORMES_VIEW' }
  }
];

import { Routes } from '@angular/router';
import { HabilidadAsociadoListComponent } from './habilidad-asociado-list.component';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

export const habilidadAsociadoRoutes: Routes = [
  {
    path: '',
    component: HabilidadAsociadoListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_HABILIDAD_ASOCIADO_EXECUTE'
    }
  }
];

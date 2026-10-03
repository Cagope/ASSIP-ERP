import { Routes } from '@angular/router';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const CDAT_LIQUIDACION_DIARIA_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./cdat-liquidacion-diaria.component')
        .then(m => m.CdatLiquidacionDiariaComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'CDAT_LIQUIDACION_DIARIA_EXECUTE'
    }
  }

];

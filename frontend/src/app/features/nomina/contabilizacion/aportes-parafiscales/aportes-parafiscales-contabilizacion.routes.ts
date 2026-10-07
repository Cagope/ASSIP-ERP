import { Routes } from '@angular/router';

import { permisoGuard } from '../../../../core/auth/permiso.guard';
import { AportesParafiscalesContabilizacionComponent } from './aportes-parafiscales-contabilizacion.component';

export const APORTES_PARAFISCALES_CONTABILIZACION_ROUTES: Routes = [
  {
    path: '',
    component: AportesParafiscalesContabilizacionComponent,
    canActivate: [permisoGuard],
    data: { permiso: 'NOMINA_CONTABILIZACION_EXECUTE' }
  }
];

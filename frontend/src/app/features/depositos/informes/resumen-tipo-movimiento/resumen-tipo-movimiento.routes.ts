import { Routes } from '@angular/router';

export const RESUMEN_TIPO_MOVIMIENTO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./resumen-tipo-movimiento.component')
        .then(m => m.ResumenTipoMovimientoComponent)
  }
];

import { Routes } from '@angular/router';

export const CDAT_CANCELACION_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./cdat-cancelacion.component')
        .then(m => m.CdatCancelacionComponent),
  }

];

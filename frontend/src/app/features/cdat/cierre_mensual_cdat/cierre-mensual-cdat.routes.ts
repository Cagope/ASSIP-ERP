import { Routes } from '@angular/router';

export const CIERRE_MENSUAL_CDAT_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./cierre-mensual-cdat.component')
        .then(m => m.CierreMensualCdatComponent),
  }

];

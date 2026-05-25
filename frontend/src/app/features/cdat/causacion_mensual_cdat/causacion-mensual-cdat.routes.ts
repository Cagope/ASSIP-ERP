import { Routes } from '@angular/router';

export const CAUSACION_MENSUAL_CDAT_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./causacion-mensual-cdat.component')
        .then(m => m.CausacionMensualCdatComponent),
  }

];

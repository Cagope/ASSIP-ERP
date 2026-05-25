import { Routes } from '@angular/router';

export const CDAT_LIQUIDACION_DIARIA_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./cdat-liquidacion-diaria.component')
        .then(m => m.CdatLiquidacionDiariaComponent),
  }

];

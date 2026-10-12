import { Routes } from '@angular/router';

export const TESORERIA_ROUTES: Routes = [

  // =========================================================
  // CONSIGNACIONES BANCARIAS
  // =========================================================
  {
    path: 'consignaciones-bancarias',
    loadComponent: () =>
      import('./consignaciones-bancarias/consignaciones-bancarias.component')
        .then(m => m.ConsignacionesBancariasComponent)
  }

];

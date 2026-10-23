import {
  Routes
} from '@angular/router';


// =========================================================
// RUTAS - PAGO DE CUOTAS V1
// =========================================================

export const PAGO_CUOTAS_V1_ROUTES: Routes = [

  // =======================================================
  // PANTALLA PRINCIPAL
  // =======================================================

  {
    path: '',
    loadComponent: () =>
      import(
        './pago-cuotas_v1.component'
      ).then(
        m => m.PagoCuotasV1Component
      )
  }

];

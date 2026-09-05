import { Routes } from '@angular/router';

import {
  CierreMensualCarteraComponent
} from './cierre-mensual-cartera.component';

export const CIERRE_MENSUAL_CARTERA_ROUTES: Routes = [

  // =========================================================
  // CONSOLIDACIÓN DEL CIERRE
  // =========================================================

  {
    path: '',
    component: CierreMensualCarteraComponent
  },

  // =========================================================
  // CÁLCULOS
  // =========================================================

  {
    path: 'calculos',
    loadComponent: () =>
      import(
        './calculos/calculos-cierre.component'
      ).then(
        m => m.CalculosCierreComponent
      )
  },

  // =========================================================
  // ANEXO 1
  // =========================================================

  {
    path: 'anexo1',
    loadComponent: () =>
      import(
        './anexo1/anexo1-cierre.component'
      ).then(
        m => m.Anexo1CierreComponent
      )
  },

  // =========================================================
  // ANEXO 2 / PÉRDIDA ESPERADA
  // =========================================================

  {
    path: 'anexo2',
    loadComponent: () =>
      import(
        './anexo2/anexo2-cierre.component'
      ).then(
        m => m.Anexo2CierreComponent
      )
  }

];

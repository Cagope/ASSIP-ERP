import { Routes } from '@angular/router';

export const CONSOLIDADO_CONCEPTOS_INFORME_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./consolidado-conceptos-informe.component')
        .then(m => m.ConsolidadoConceptosInformeComponent)
  }
];

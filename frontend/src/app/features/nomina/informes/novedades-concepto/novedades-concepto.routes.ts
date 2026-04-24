import { Routes } from '@angular/router';

export const NOVEDADES_CONCEPTO_INFORME_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./novedades-concepto-informe.component')
        .then(m => m.NovedadesConceptoInformeComponent)
  }
];

import { Routes } from '@angular/router';

export const CUMPLEANIOS_ASOCIADOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./cumpleanios-asociados.component')
        .then(m => m.CumpleaniosAsociadosComponent)
  }
];

import { Routes } from '@angular/router';

export const GMF_SEMANAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./gmf-semanal.component')
        .then(m => m.GmfSemanalComponent)
  }
];

import { Routes } from '@angular/router';

export const CONSULTA_CDATS_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./consulta-cdats-list.component')
        .then(m => m.ConsultaCdatsListComponent),
  }

];

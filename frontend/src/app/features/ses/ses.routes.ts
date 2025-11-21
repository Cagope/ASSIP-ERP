import { Routes } from '@angular/router';

export const sesRoutes: Routes = [
  {
    path: 'asociados',
    loadComponent: () =>
      import('./asociados/asociados-list.component').then(m => m.AsociadosListComponent)
  },
  {
    path: 'aportes',
    loadComponent: () =>
      import('./aportes/aportes-list.component').then(m => m.AportesListComponent)
  }
];

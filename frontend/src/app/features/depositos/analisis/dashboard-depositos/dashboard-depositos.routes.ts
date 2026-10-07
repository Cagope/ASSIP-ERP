import { Routes } from '@angular/router';

export const dashboardDepositosRoutes: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./dashboard-depositos.component')
        .then(m => m.DashboardDepositosComponent)
  }

];

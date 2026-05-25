import { Routes } from '@angular/router';

export const GERENCIA_ROUTES: Routes = [

  {
    path: '',
    redirectTo: 'dashboard-cdat',
    pathMatch: 'full'
  },

  {
    path: 'dashboard-cdat',
    loadComponent: () =>
      import('./dashboards/dashboard-cdat/dashboard-cdat.component')
        .then(m => m.DashboardCdatComponent)
  },

  {
    path: 'dashboard-depositos',
    loadChildren: () =>
      import('./dashboards/dashboard-depositos/dashboard-depositos.routes')
        .then(m => m.dashboardDepositosRoutes)
  }

];

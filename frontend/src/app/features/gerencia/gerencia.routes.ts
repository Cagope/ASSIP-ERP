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
    path: 'dashboard-cartera',
    loadComponent: () =>
      import('./dashboards/dashboard-cartera/dashboard-cartera.component')
        .then(m => m.DashboardCarteraComponent)
  },

  {
    path: 'dashboard-depositos',
    loadChildren: () =>
      import('./dashboards/dashboard-depositos/dashboard-depositos.routes')
        .then(m => m.dashboardDepositosRoutes)
  },

  {
    path: 'expediente-asociado',
    loadChildren: () =>
      import('./expediente-asociado/expediente-asociado.routes')
        .then(m => m.EXPEDIENTE_ASOCIADO_ROUTES)
  }

];

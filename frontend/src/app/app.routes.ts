import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { LoginComponent } from './core/auth/login/login.component';
import { MainLayoutComponent } from './layout/main-layout.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],

    children: [

      // === GENERAL ===
      {
        path: 'general',
        loadChildren: () =>
          import('./features/general/general.routes')
            .then(m => m.GENERAL_ROUTES),
      },

      // === HOJA DE VIDA ===
      {
        path: 'hoja-vida',
        loadChildren: () =>
          import('./features/hoja-vida/hoja-vida.routes')
            .then(m => m.HOJA_VIDA_ROUTES),
      },

      // === DEPÓSITOS ===
      {
        path: 'depositos',
        loadChildren: () =>
          import('./features/depositos/depositos.routes')
            .then(m => m.DEPOSITOS_ROUTES),
      },

      // === CDAT ===
      {
        path: 'cdat',
        loadChildren: () =>
          import('./features/cdat/cdat.routes')
            .then(m => m.CDAT_ROUTES),
      },

      // === SARLAFT ===
      {
        path: 'sarlaft',
        loadChildren: () =>
          import('./features/sarlaft/sarlaft.routes')
            .then(m => m.SARLAFT_ROUTES),
      },

      // === SEGURIDAD ===
      {
        path: 'seguridad',
        loadChildren: () =>
          import('./features/seguridad/seguridad.routes')
            .then(m => m.SEGURIDAD_ROUTES),
      },

      // === CONTABILIDAD ===
      {
        path: 'contabilidad',
        loadChildren: () =>
          import('./features/contabilidad/contabilidad.routes')
            .then(m => m.CONTABILIDAD_ROUTES),
      },

      // === ACTIVOS FIJOS ===
      {
        path: 'activos-fijos',
        loadChildren: () =>
          import('./features/activos-fijos/activos-fijos.routes')
            .then(m => m.ACTIVOS_FIJOS_ROUTES),
      },

      // === NÓMINA ===
      {
        path: 'nomina',
        loadChildren: () =>
          import('./features/nomina/nomina.routes')
            .then(m => m.NOMINA_ROUTES),
      },

      // === SUPERINTENDENCIA SES ===
      {
        path: 'ses',
        loadChildren: () =>
          import('./features/ses/ses.routes')
            .then(m => m.sesRoutes),   // ✔️ LA LÍNEA CORRECTA
      },

    ],
  },

  { path: '**', redirectTo: '' },
];

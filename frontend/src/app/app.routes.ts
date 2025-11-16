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
      {
        path: 'general',
        loadChildren: () =>
          import('./features/general/general.routes')
            .then(m => m.GENERAL_ROUTES),
      },
      {
        path: 'hoja-vida',
        loadChildren: () =>
          import('./features/hoja-vida/hoja-vida.routes')
            .then(m => m.HOJA_VIDA_ROUTES),
      },
      {
        path: 'depositos',
        loadChildren: () =>
          import('./features/depositos/depositos.routes')
            .then(m => m.DEPOSITOS_ROUTES),
      },

      {
        path: 'sarlaft',
        loadChildren: () =>
          import('./features/sarlaft/sarlaft.routes')
            .then(m => m.SARLAFT_ROUTES),
      },
      // 👇 futuros esquemas
      // {
      //   path: 'contabilidad',
      //   loadChildren: () =>
      //     import('./features/contabilidad/contabilidad.routes')
      //       .then(m => m.CONTABILIDAD_ROUTES),
      // },
      // {
      //   path: 'activos-fijos',
      //   loadChildren: () =>
      //     import('./features/activos-fijos/activos-fijos.routes')
      //       .then(m => m.ACTIVOS_FIJOS_ROUTES),
      // },
    ],
  },
  { path: '**', redirectTo: '' },
];

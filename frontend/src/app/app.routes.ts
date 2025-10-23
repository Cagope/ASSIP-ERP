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
        path: 'hoja-vida',
        canActivate: [authGuard],
        children: [
          { path: 'datos-personales', loadComponent: () => import('./features/hoja-vida/datos-personales/datos-personales.component').then(m => m.DatosPersonalesComponent) },
          { path: 'ubicaciones', loadComponent: () => import('./features/hoja-vida/ubicaciones/ubicaciones.component').then(m => m.UbicacionesComponent) },
          { path: 'economicos', loadComponent: () => import('./features/hoja-vida/economicos/economicos.component').then(m => m.EconomicosComponent) },
          { path: 'referencias', loadComponent: () => import('./features/hoja-vida/referencias/referencias.component').then(m => m.ReferenciasComponent) },
          { path: 'familiares', loadComponent: () => import('./features/hoja-vida/familiares/familiares.component').then(m => m.FamiliaresComponent) },
          { path: '**', redirectTo: 'datos-personales' }
        ]
      }
    ],
  },
  { path: '**', redirectTo: '' },
];

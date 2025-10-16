import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

// Shell (menu lateral)
import { MenuComponent } from './features/menu/menu.component';
// Presentación (portada central)
import { PresentacionComponent } from './shared/general/presentacion/presentacion.component';

export const routes: Routes = [
  // --- Login (fuera del shell) ---
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login.component').then(m => m.LoginComponent),
  },

  // --- Cambiar password (fuera del shell) ---
  {
    path: 'auth/cambiar-password',
    loadComponent: () =>
      import('./features/seguridad/cambiar-password/cambiar-password.component')
        .then(m => m.CambiarPasswordComponent),
  },

  // --- Impresiones (fuera del shell) ---
  {
    path: 'general/zonas/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/general/zonas/zonas.routes')
        .then(m => m.ZONAS_PRINT_ROUTES),
  },
  {
    path: 'general/sub-zonas/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/general/sub-zonas/sub-zonas.routes')
        .then(m => m.SUBZONAS_PRINT_ROUTES),
  },
  {
    path: 'general/parametros/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/general/parametros/parametros.routes')
        .then(m => m.PARAMETROS_PRINT_ROUTES),
  },
  {
    path: 'hoja-vida/datos-personales/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/hoja-vida/datos-personales/datos-personales.routes')
        .then(m => m.DATOS_PERSONALES_PRINT_ROUTES),
  },
  {
    path: 'hoja-vida/datos-familiares/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/hoja-vida/datos-familiares/datos-familiares.routes')
        .then(m => m.DATOS_FAMILIARES_PRINT_ROUTES),
  },
  {
    path: 'hoja-vida/referencias-personales/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/hoja-vida/referencias-personales/referencias-personales.routes')
        .then(m => m.REFERENCIAS_PERSONALES_PRINT_ROUTES),
  },
  {
    path: 'general/directivos/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/general/directivos/directivos.routes')
        .then(m => m.DIRECTIVOS_PRINT_ROUTES),
  },
  {
    path: 'general/privilegiados/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/general/privilegiados/privilegiados.routes')
        .then(m => m.PRIVILEGIADOS_PRINT_ROUTES),
  },
  {
    path: 'hoja-vida/ubicaciones/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/hoja-vida/ubicaciones/ubicaciones.routes')
        .then(m => m.UBICACIONES_PRINT_ROUTES),
  },
  {
    path: 'hoja-vida/permisos-especiales/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/hoja-vida/permisos-especiales/permisos-especiales.routes')
        .then(m => m.PERMISOS_ESPECIALES_PRINT_ROUTES),
  },
  {
    path: 'hoja-vida/financieros/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/hoja-vida/financieros/financieros.routes')
        .then(m => m.FINANCIEROS_PRINT_ROUTES),
  },
  {
    path: 'hoja-vida/sarlaft/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/hoja-vida/sarlaft/sarlaft.routes')
        .then(m => m.SARLAFT_PRINT_ROUTES),
  },
  {
    path: 'hoja-vida/laborales/print',
    canMatch: [authGuard],
    loadChildren: () =>
      import('./features/hoja-vida/laborales/laborales.routes')
        .then(m => m.LABORALES_PRINT_ROUTES),
  },

  // --- Shell autenticado: MENÚ + CONTENIDO ---
  {
    path: '',
    component: MenuComponent,
    canMatch: [authGuard],
    children: [
      { path: '', component: PresentacionComponent },

      {
        path: 'general/agencias',
        loadChildren: () =>
          import('./features/general/agencias/agencias.routes')
            .then(m => m.AGENCIAS_ROUTES),
      },
      {
        path: 'general/zonas',
        loadChildren: () =>
          import('./features/general/zonas/zonas.routes')
            .then(m => m.ZONAS_ROUTES),
      },
      {
        path: 'general/sub-zonas',
        loadChildren: () =>
          import('./features/general/sub-zonas/sub-zonas.routes')
            .then(m => m.SUBZONAS_ROUTES),
      },
      {
        path: 'general/parametros',
        loadChildren: () =>
          import('./features/general/parametros/parametros.routes')
            .then(m => m.PARAMETROS_ROUTES),
      },
      {
        path: 'general/directivos',
        canMatch: [authGuard],
        loadChildren: () =>
          import('./features/general/directivos/directivos.routes')
            .then(m => m.DIRECTIVOS_ROUTES),
      },
      {
        path: 'general/privilegiados',
        canMatch: [authGuard],
        loadChildren: () =>
          import('./features/general/privilegiados/privilegiados.routes')
            .then(m => m.PRIVILEGIADOS_ROUTES),
      },

      {
        path: 'hoja-vida/datos-personales',
        loadChildren: () =>
          import('./features/hoja-vida/datos-personales/datos-personales.routes')
            .then(m => m.DATOS_PERSONALES_ROUTES),
      },
      {
        path: 'hoja-vida/datos-familiares',
        loadChildren: () =>
          import('./features/hoja-vida/datos-familiares/datos-familiares.routes')
            .then(m => m.DATOS_FAMILIARES_ROUTES),
      },
      {
        path: 'hoja-vida/referencias-personales',
        loadChildren: () =>
          import('./features/hoja-vida/referencias-personales/referencias-personales.routes')
            .then(m => m.REFERENCIAS_PERSONALES_ROUTES),
      },
      {
        path: 'hoja-vida/ubicaciones',
        loadChildren: () =>
          import('./features/hoja-vida/ubicaciones/ubicaciones.routes')
            .then(m => m.UBICACIONES_ROUTES),
      },
      {
        path: 'hoja-vida/permisos-especiales',
        loadChildren: () =>
          import('./features/hoja-vida/permisos-especiales/permisos-especiales.routes')
            .then(m => m.PERMISOS_ESPECIALES_ROUTES),
      },
      {
        path: 'hoja-vida/financieros',
        loadChildren: () =>
          import('./features/hoja-vida/financieros/financieros.routes')
            .then(m => m.FINANCIEROS_ROUTES),
      },
      {
        path: 'hoja-vida/sarlaft',
        loadChildren: () =>
          import('./features/hoja-vida/sarlaft/sarlaft.routes')
            .then(m => m.SARLAFT_ROUTES),
      },
      {
        path: 'hoja-vida/laborales',
        loadChildren: () =>
          import('./features/hoja-vida/laborales/laborales.routes')
            .then(m => m.LABORALES_ROUTES),
      },

      // 🔹 Wizard Captura (DENTRO del Shell)
      {
        path: 'hoja-vida/captura',
        canMatch: [authGuard],
        loadChildren: () =>
          import('./features/hoja-vida/captura_global/captura.routes')
            .then(m => m.CAPTURA_ROUTES),
      },

      {
        path: 'seguridad/usuarios',
        loadComponent: () =>
          import('./features/seguridad/usuarios/usuarios-list.component')
            .then(m => m.UsuariosListComponent),
      },
    ],
  },

  // Catch-all
  { path: '**', redirectTo: 'login' },
];

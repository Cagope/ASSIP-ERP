import { Routes } from '@angular/router';

export const SARLAFT_ROUTES: Routes = [

  // 🟧 Informe: Personas Desactualizadas
  {
    path: 'informe-desactualizados',
    loadComponent: () =>
      import('./informes/actualizacion-list.component')
        .then(m => m.ActualizacionListComponent),
  },

  // 🟦 Informe: Datos Demográficos
  {
    path: 'informe-demograficos',
    loadComponent: () =>
      import('./informes/demograficos-list.component')
        .then(m => m.DemograficosListComponent),
  },

  // 🟥 Informe: Movimientos Inusuales
  {
    path: 'informe-movimientos-inusuales',
    loadComponent: () =>
      import('./informes/movimientos-inusuales-list.component')
        .then(m => m.MovimientosInusualesListComponent),
  },

  // 🟪 Regla 002 — Documento vs Edad
  {
    path: 'regla-002',
    loadComponent: () =>
      import('./informes/regla002-list.component')
        .then(m => m.Regla002ListComponent),
  },

  // 🟨 Regla 003 — Forma 03 prohibida para mayores
  {
    path: 'regla-003',
    loadComponent: () =>
      import('./informes/regla003-list.component')
        .then(m => m.Regla003ListComponent),
  },

  // 🟩 Redirección por defecto
  {
    path: '',
    redirectTo: 'informe-desactualizados',
    pathMatch: 'full',
  }

];

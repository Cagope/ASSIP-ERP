import { Routes } from '@angular/router';

export const SARLAFT_ROUTES: Routes = [

  // 🟧 Informe: Personas Desactualizadas
  {
    path: 'actualizacion',
    loadComponent: () =>
      import('./informes/actualizacion/actualizacion-list.component')
        .then(m => m.ActualizacionListComponent),
  },

  // 🟦 Informe: Datos Demográficos
  {
    path: 'demograficos',
    loadComponent: () =>
      import('./informes/demograficos/demograficos-list.component')
        .then(m => m.DemograficosListComponent),
  },

  // 🟥 Informe: Movimientos Inusuales
  {
    path: 'movimientos-inusuales',
    loadComponent: () =>
      import('./informes/movimientos-inusuales/movimientos-inusuales-list.component')
        .then(m => m.MovimientosInusualesListComponent),
  },

  // 🟪 Regla 002 — Documento vs Edad
  {
    path: 'regla-002',
    loadComponent: () =>
      import('./informes/regla002/regla002-list.component')
        .then(m => m.Regla002ListComponent),
  },

  // 🟨 Regla 003 — Forma 03 prohibida para mayores
  {
    path: 'regla-003',
    loadComponent: () =>
      import('./informes/regla003/regla003-list.component')
        .then(m => m.Regla003ListComponent),
  },

  // 🟩 Redirección por defecto
  {
    path: '',
    redirectTo: 'actualizacion',
    pathMatch: 'full',
  }

];

import { Routes } from '@angular/router';

export const DOCUMENTOS_SOPORTE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./documentos-soporte.component')
        .then(m => m.DocumentosSoporteComponent)
  }
];

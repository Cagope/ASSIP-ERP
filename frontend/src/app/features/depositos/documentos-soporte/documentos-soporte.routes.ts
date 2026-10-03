import { Routes } from '@angular/router';
import { permisoGuard } from '../../../core/auth/permiso.guard';

export const DOCUMENTOS_SOPORTE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./documentos-soporte.component')
        .then(m => m.DocumentosSoporteComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_DOCUMENTOS_SOPORTE_VIEW'
    }
  }
];

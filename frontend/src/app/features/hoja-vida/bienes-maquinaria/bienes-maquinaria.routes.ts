import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { BienesMaquinariaListComponent } from './bienes-maquinaria-list.component';

export const BIENES_MAQUINARIA_ROUTES: Routes = [
  {
    path: '',
    component: BienesMaquinariaListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_BIENES_VIEW'
    }
  },
  {
    path: 'persona/:idDatosPersonal/gestionar',
    loadComponent: () =>
      import('./bienes-maquinaria-upsert.component')
        .then(m => m.BienesMaquinariaUpsertComponent),
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_BIENES_VIEW'
    }
  }
];

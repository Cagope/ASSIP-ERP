import { Routes } from '@angular/router';

import { BienesMaquinariaListComponent } from './bienes-maquinaria-list.component';

export const BIENES_MAQUINARIA_ROUTES: Routes = [
  {
    path: '',
    component: BienesMaquinariaListComponent
  },
  {
    path: 'persona/:idDatosPersonal/gestionar',
    loadComponent: () =>
      import('./bienes-maquinaria-upsert.component')
        .then(m => m.BienesMaquinariaUpsertComponent)
  }
];

import { Routes } from '@angular/router';

export const EXPEDIENTE_ASOCIADO_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./list/expediente-asociado-list.component')
        .then(m => m.ExpedienteAsociadoListComponent)
  },

  {
    path: ':idDatosPersonal',
    loadComponent: () =>
      import('./expediente-asociado.component')
        .then(m => m.ExpedienteAsociadoComponent)
  }

];

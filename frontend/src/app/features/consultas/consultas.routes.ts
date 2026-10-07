import { Routes } from '@angular/router';

export const CONSULTAS_ROUTES: Routes = [

  {
    path: 'expediente-asociado',
    loadChildren: () =>
      import('./expediente-asociado/expediente-asociado.routes')
        .then(m => m.EXPEDIENTE_ASOCIADO_ROUTES)
  }

];

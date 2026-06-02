import { Routes } from '@angular/router';

export const VINCULAR_CAJA_ROUTES: Routes = [

  {
    path: '',
    loadComponent: () =>
      import('./vincular-caja.component')
        .then(m => m.VincularCajaComponent),
  }

];

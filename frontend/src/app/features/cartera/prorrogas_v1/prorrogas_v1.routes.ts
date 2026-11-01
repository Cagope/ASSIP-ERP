import { Routes } from '@angular/router';

/** Ruta hija para incorporar en CARTERA_ROUTES con permiso funcional propio. */
export const PRORROGAS_V1_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./prorrogas_v1.component').then(m => m.ProrrogasV1Component)
  }
];

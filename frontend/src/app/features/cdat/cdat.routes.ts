import { Routes } from '@angular/router';

// CDAT CRUD
import { CDATS_ROUTES } from './cdats/cdats.routes';

export const CDAT_ROUTES: Routes = [

  // =============================
  // OPERACIÓN
  // =============================
  {
    path: 'cdats',
    children: CDATS_ROUTES
  }

];

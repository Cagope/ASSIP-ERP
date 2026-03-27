import { Routes } from '@angular/router';
import { DesprendibleComponent } from './desprendible.component';

export const DESPRENDIBLE_ROUTES: Routes = [
  {
    path: ':idPeriodo',
    component: DesprendibleComponent,
  }
];

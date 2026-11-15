import { Routes } from '@angular/router';
import { AgenciaListComponent } from './agencia-list.component';
import { AgenciaUpsertComponent } from './agencia-upsert.component';

import { permisoGuard } from '../../../core/auth/permiso.guard';

export const AGENCIA_ROUTES: Routes = [
  { path: '', component: AgenciaListComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_AGENCIAS_VIEW' } },
  { path: 'nueva', component: AgenciaUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_AGENCIAS_EDIT' } },
  { path: ':id', component: AgenciaUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'GENERAL_AGENCIAS_EDIT' } },
];

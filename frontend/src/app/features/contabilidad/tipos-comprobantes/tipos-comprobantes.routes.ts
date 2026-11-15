import { Routes } from '@angular/router';
import { permisoGuard } from '../../../core/auth/permiso.guard';
import { TiposComprobantesListComponent } from './tipos-comprobantes-list.component';
import { TiposComprobantesUpsertComponent } from './tipos-comprobantes-upsert.component';

export const TIPOS_COMPROBANTES_ROUTES: Routes = [
  { path: '', component: TiposComprobantesListComponent },
  { path: 'nuevo', component: TiposComprobantesUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'CONTABILIDAD_PARAMETRIZACION_ADMIN' } },
  { path: ':tipo/:idAgencia/editar', component: TiposComprobantesUpsertComponent, canActivate: [permisoGuard], data: { permiso: 'CONTABILIDAD_PARAMETRIZACION_ADMIN' } },
];

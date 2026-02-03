import { Routes } from '@angular/router';
import { TiposComprobantesListComponent } from './tipos-comprobantes-list.component';
import { TiposComprobantesUpsertComponent } from './tipos-comprobantes-upsert.component';

export const TIPOS_COMPROBANTES_ROUTES: Routes = [
  { path: '', component: TiposComprobantesListComponent },
  { path: 'nuevo', component: TiposComprobantesUpsertComponent },
  { path: ':tipo/:idAgencia/editar', component: TiposComprobantesUpsertComponent },
];

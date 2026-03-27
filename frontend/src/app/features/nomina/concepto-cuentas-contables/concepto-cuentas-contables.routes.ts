import { Routes } from '@angular/router';
import { ConceptoCuentasContablesListComponent } from './concepto-cuentas-contables-list.component';
import { ConceptoCuentasContablesUpsertComponent } from './concepto-cuentas-contables-upsert.component';

export const CONCEPTO_CUENTAS_CONTABLES_ROUTES: Routes = [
  { path: '', component: ConceptoCuentasContablesListComponent },
  { path: 'nuevo', component: ConceptoCuentasContablesUpsertComponent },
  { path: ':idMapeo/editar', component: ConceptoCuentasContablesUpsertComponent },
];

import { Routes } from '@angular/router';
import { ConceptosNominaListComponent } from './conceptos-nomina-list.component';
import { ConceptosNominaUpsertComponent } from './conceptos-nomina-upsert.component';

export const CONCEPTOS_NOMINA_ROUTES: Routes = [
  { path: '', component: ConceptosNominaListComponent },
  { path: 'nuevo', component: ConceptosNominaUpsertComponent },
  { path: ':codigo/editar', component: ConceptosNominaUpsertComponent },
];

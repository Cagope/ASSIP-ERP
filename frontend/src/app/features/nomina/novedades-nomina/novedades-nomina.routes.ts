import { Routes } from '@angular/router';
import { NovedadesNominaListComponent } from './novedades-nomina-list.component';
import { NovedadesNominaUpsertComponent } from './novedades-nomina-upsert.component';
import { NovedadesNominaMasivoComponent } from './novedades-nomina-masivo.component'; // 👈 NUEVO

export const NOVEDADES_NOMINA_ROUTES: Routes = [
  { path: '', component: NovedadesNominaListComponent },
  { path: 'nuevo', component: NovedadesNominaUpsertComponent },
  { path: 'masivo', component: NovedadesNominaMasivoComponent }, // 👈 NUEVO
  { path: ':id', component: NovedadesNominaUpsertComponent },
];

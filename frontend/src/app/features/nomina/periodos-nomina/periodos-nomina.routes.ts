import { Routes } from '@angular/router';

import { PeriodosNominaListComponent } from './periodos-nomina-list.component';
import { PeriodosGeneradorComponent } from './periodos-generador.component';
import { NovedadesNominaMasivoComponent } from '../novedades-nomina/novedades-nomina-masivo.component';

export const PERIODOS_NOMINA_ROUTES: Routes = [

  { path: '', component: PeriodosNominaListComponent },

  { path: 'generar', component: PeriodosGeneradorComponent },

  { path: 'masivo', component: NovedadesNominaMasivoComponent },

];

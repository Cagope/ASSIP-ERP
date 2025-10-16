import { Routes } from '@angular/router';
import { CapturaShellComponent } from './captura-shell.component';
import { CapturaGuard } from './captura.guard';

// Pasos (wrappers). Por ahora implementamos el de Datos Personales real y el Resumen.
// El resto quedan como placeholders y los vas reemplazando a medida que envuelves cada upsert.
import { PasoDatosPersonalesComponent } from './pasos/paso-datos-personales.component';
import { PasoResumenComponent } from './pasos/paso-resumen.component';
import { PasoUbicacionesComponent } from './pasos/paso-ubicaciones.component';
import { PasoLaboralesComponent } from './pasos/paso-laborales.component';
import { PasoFinancierosComponent } from './pasos/paso-financieros.component';
import { PasoDatosFamiliaresComponent } from './pasos/paso-datos-familiares.component';
import { PasoReferenciasComponent } from './pasos/paso-referencias.component';
import { PasoSarlaftComponent } from './pasos/paso-sarlaft.component';
import { PasoPermisosComponent } from './pasos/paso-permisos.component';

export const CAPTURA_ROUTES: Routes = [
  {
    path: '',
    component: CapturaShellComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'datos-personales' },
      { path: 'datos-personales', component: PasoDatosPersonalesComponent },
      { path: 'ubicaciones',      component: PasoUbicacionesComponent },
      { path: 'laborales',        component: PasoLaboralesComponent },
      { path: 'financieros',      component: PasoFinancierosComponent },
      { path: 'datos-familiares', component: PasoDatosFamiliaresComponent },
      { path: 'referencias',      component: PasoReferenciasComponent },
      { path: 'sarlaft',          component: PasoSarlaftComponent },
      { path: 'permisos',         component: PasoPermisosComponent },
      { path: 'resumen',          component: PasoResumenComponent, canActivate: [CapturaGuard] },
    ],
  },
];

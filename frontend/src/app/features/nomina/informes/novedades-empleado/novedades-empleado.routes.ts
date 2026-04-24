import { Routes } from '@angular/router';

export const NOVEDADES_EMPLEADO_INFORME_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./novedades-empleado-informe.component')
        .then(m => m.NovedadesEmpleadoInformeComponent)
  }
];
